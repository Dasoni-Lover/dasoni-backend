package dasoni_backend.domain.hall.service;

import dasoni_backend.domain.hall.converter.HallConverter;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailDataResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchResponseListDTO;
import dasoni_backend.domain.hall.dto.HallDTO.MyHallResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallQueryRepository;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.relationship.converter.RelationshipConverter;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.relationship.entity.RelationshipNature;
import dasoni_backend.domain.relationship.repository.relationshipNatureRepository;
import dasoni_backend.domain.relationship.repository.relationshipRepository;
import dasoni_backend.domain.request.entity.Request;
import dasoni_backend.domain.request.repository.RequestRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.dto.VoiceDTOs.VoiceDTO;
import dasoni_backend.domain.voice.entity.Voice;
import dasoni_backend.global.S3.service.FileUploadService;
import dasoni_backend.global.enums.HallStatus;
import dasoni_backend.global.enums.Personality;
import dasoni_backend.global.enums.RequestStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static dasoni_backend.domain.hall.converter.HallConverter.toSearchResponseDTO;
import static dasoni_backend.domain.hall.converter.HallConverter.toSearchResponseListDTO;

@Slf4j
@Service
@RequiredArgsConstructor // 자동 생성자 주입
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final HallQueryRepository hallQueryRepository;
    private final relationshipRepository relationshipRepository;
    private final relationshipNatureRepository relationshipNatureRepository;
    private final RequestRepository requestRepository;

    @Transactional(readOnly = true)
    @Override
    public HallListResponseDTO getHomeHallList(User user) {
        // 로그인하지 않았을 경우, 빈 리스트 반환(수정 불가능)
        if (user == null)
            return HallConverter.toHallListResponseDTO(List.of());

        List<Hall> halls = hallRepository.findAllByFollowerUserIdOrderByCreatedAtDesc(user.getId());
        return HallConverter.toHallListResponseDTO(halls);
    }

    @Transactional(readOnly = true)
    @Override
    public HallListResponseDTO getManageHallList(User admin) {
        // 관리자 ID가 없을 경우(로그인 x), 빈 리스트 반환(수정 불가능)
        if (admin.getId() == null)
            return HallConverter.toHallListResponseDTO(List.of());

        List<Hall> halls = hallRepository.findAllManagedHallsExceptSelf(admin.getId());
        return HallConverter.toHallListResponseDTO(halls);
    }

    @Transactional(readOnly = true)
    @Override
    public SidebarResponseDTO getSidebar(User user) {
        // notiCount는 추후 알림 연동 예정 : 임시로 0으로 반환
        return SidebarResponseDTO.builder()
                .name(user.getName())
                .myProfile(user.getMyProfile())
                .notiCount(0)
                .build();
    }

    // 본인 추모관 개설
    @Transactional
    @Override
    public HallCreateResponseDTO createMyHall(User user) {
        // 본인 추모관 존재 O -> 중복 생성 방지
        // 본인 추모관 존재 X -> 생성 후, hallId 반환
        return hallRepository.findBySubjectId(user.getId())
            // O
            .map(existing -> HallCreateResponseDTO.builder()
                    .hallId(existing.getId())
                    .build())
            // X
            .orElseGet(() -> {
                Hall hall = hallRepository.save(HallConverter.fromSaveRequest(user));
                return HallCreateResponseDTO.builder()
                        .hallId(hall.getId())
                        .build();
            });
    }

    // 타인 추모관 개설
    @Transactional
    @Override
    public HallCreateResponseDTO createOtherHall(User admin, HallCreateRequestDTO request) {
        // 타인 추모관 개설
        Hall hall = hallRepository.save(HallConverter.fromSaveRequestForOther(admin, request));
        Relationship relationship = relationshipRepository.save(RelationshipConverter.fromRequestToRelationship(admin, hall, request));

        List<RelationshipNature> rows = request.getNatures().stream()
                .map(p -> RelationshipNature.builder()
                        .relationship(relationship)
                        .nature(p)
                        .build())
                .toList();

        relationshipNatureRepository.saveAll(rows);
        return HallConverter.toHallCreateResponseDTO(hall);
    }

    @Transactional(readOnly = true)
    @Override
    public HallDetailDataResponseDTO getHallDetail(Long hallId, User user) {

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 추모관입니다."));

        Long userId = user.getId();
        Long adminId = hall.getAdmin() != null ? hall.getAdmin().getId() : null;
        Long subjectId = hall.getSubjectId();

        // role 나누기
        String role;
        if (Objects.equals(userId, subjectId)) {
            role = "me";
        } else if (Objects.equals(userId, adminId)) {
            role = "admin";
        } else {
            role = "follower";
        }

        // 상위 4개 natures 전시
        List<String> top4Natures = hallQueryRepository.findTop4NatureNames(hallId);

        List<String> result = top4Natures.stream()
                .map(e -> Personality.valueOf(e).getValue()) // getValue() = 한글 문자열
                .toList();

        log.info("getHallDetail hallId={}, userId={}, adminId={}, subjectId={}",
                hallId, user.getId(),
                hall.getAdmin() != null ? hall.getAdmin().getId() : null,
                hall.getSubjectId());

        // 변환(me일때는 필요없는 null처리 )
        return HallConverter.toHallDetailResponse(hall, role, result);
    }
    @Override
    @Transactional(readOnly = true)
    public MyHallResponseDTO getMyHall(User user){
        Hall hall = hallRepository.findBySubjectId(user.getId()).orElse(null);
        return HallConverter.toMyHallResponse(hall);
    }

    @Override
    @Transactional
    public HallSearchResponseListDTO searchHalls(HallSearchRequestDTO requestDTO, User user) {

        LocalDate birthday = parseDate(requestDTO.getBirthday());
        LocalDate deadDay = parseDate(requestDTO.getDeadDay());

        // Repository에서 검색 (isSecret=false 자동 필터링)
        List<Hall> halls = hallRepository.searchHalls(
                requestDTO.getName(),
                birthday,
                deadDay
        );

        // DTO 변환 및 status 설정
        List<HallSearchResponseDTO> responseDTOs = halls.stream()
                .map(hall -> {
                    HallStatus status = determineHallStatus(hall,user);
                    return toSearchResponseDTO(hall, status);
                })
                .collect(Collectors.toList());
        return toSearchResponseListDTO(responseDTOs);
    }

    private HallStatus determineHallStatus(Hall hall, User user) {
        // 1. Relationship 확인 - 이미 참여 중인가?
        boolean hasRelationship = relationshipRepository.existsByHallAndUser(hall, user);

        if (hasRelationship) {
            return HallStatus.ENTERING;  // 관계가 있음 = 입장 중
        }

        // 2. Request 확인 - 요청을 보냈는가?
        Optional<Request> pendingRequest = requestRepository
                .findByHallAndUserAndStatus(hall, user, RequestStatus.PENDING);

        if (pendingRequest.isPresent()) {
            return HallStatus.WAITING;  // 요청 대기 중
        }

        // 3. 아무 관계도 없음
        return HallStatus.NONE;
    }

    // 날짜 파싱 헬퍼 메서드
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) { return null; }
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}