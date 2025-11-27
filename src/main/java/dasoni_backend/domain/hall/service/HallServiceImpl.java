package dasoni_backend.domain.hall.service;

import dasoni_backend.domain.hall.converter.HallConverter;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailDataResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallSearchResponseListDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallUpdateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.MyHallResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallQueryRepository;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.relationship.converter.RelationshipConverter;
import dasoni_backend.domain.relationship.entity.Relationship;
import dasoni_backend.domain.relationship.repository.RelationshipRepository;
import dasoni_backend.domain.request.converter.RequestConverter;
import dasoni_backend.domain.request.dto.RequestDTO.RequestListResponseDTO;
import dasoni_backend.domain.request.entity.Request;
import dasoni_backend.domain.request.repository.RequestRepository;
import dasoni_backend.domain.user.converter.UserConverter;
import dasoni_backend.domain.user.dto.UserDTO.ProfileRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.VisitorListResponseDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.user.repository.UserRepository;
import dasoni_backend.global.S3.service.FileUploadService;
import dasoni_backend.global.enums.HallStatus;
import dasoni_backend.global.enums.Personality;
import dasoni_backend.global.enums.RequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
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
    private final RelationshipRepository relationshipRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    private final UserConverter userConverter;
    private final RequestConverter requestConverter;
    private final FileUploadService fileUploadService;


    @Transactional(readOnly = true)
    @Override
    public HallListResponseDTO getHomeHallList(User user) {
        if (user == null)
            return HallConverter.toHallListResponseDTO(List.of());
    // relationships 조회
        List<Relationship> relationships = relationshipRepository.findByUser(user);

    // relationships에서 hall들을 추출
        List<Hall> halls = relationships.stream()
                .map(Relationship::getHall)  // 각 Relationship에서 Hall을 가져옴
                .filter(Objects::nonNull)  // null 체크 (안전성)
                .collect(Collectors.toList());

    // DTO로 변환하여 반환
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

        // Relationship 저장 시 natures도 함께 저장됨 (JPA가 자동 처리)
        relationshipRepository.save(RelationshipConverter.fromRequestToRelationship(admin, hall, request));

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
    public HallSearchResponseListDTO searchHallsExceptMine(HallSearchRequestDTO requestDTO, User user) {

        LocalDate birthday = parseDate(requestDTO.getBirthday());
        LocalDate deadDay = parseDate(requestDTO.getDeadDay());

        // Repository에서 검색 (isSecret=false 자동 필터링)
        List<Hall> halls = hallRepository.searchHallsExceptMine(
                requestDTO.getName(),
                birthday,
                deadDay,
                user.getId()
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


    // 방문자 조회
    @Override
    @Transactional
    public VisitorListResponseDTO getVisitors(Long hallId, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("추모관을 찾을 수 없습니다."));
        if (!hall.getAdmin().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        List<Relationship> relationships = relationshipRepository.findByHallAndUserNot(hall,user);
        return userConverter.toVisitorListResponseDTO(relationships);
    }
    // 방문자 내보내기
    @Override
    @Transactional
    public void getOutVisitor(Long hallId, Long visitorId, User user){

        Hall hall = hallRepository.findById(hallId).orElseThrow(()->new NoSuchElementException("추모관 존재X"));

        if(!hall.getAdmin().getId().equals(user.getId())){
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        User visitor = userRepository.findById(visitorId).orElseThrow(()->new NoSuchElementException(("방문자 존재X")));

        Relationship relationship = relationshipRepository.findByHallAndUser(hall,visitor)
                .orElseThrow(() -> new IllegalArgumentException("관계가 없습니다."));

        relationshipRepository.delete(relationship);
    }


    @Override
    @Transactional
    public void updateProfile(ProfileRequestDTO request, User user){
        Hall hall = hallRepository.findBySubjectId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("본인 추모관이 없습니다."));
        updateProfileHall(hall,request.getProfile());
        user.setMyProfile(request.getProfile());
        userRepository.save(user);
    }

    // 추모관 정보 수정
    @Override
    @Transactional
    public void updateHall(Long hallId, HallUpdateRequestDTO request, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("추모관을 찾을 수 없습니다."));
        updateProfileHall(hall,request.getProfile());

        hall.setName(request.getName());
        hall.setBirthday(parseDate(request.getBirthday()));
        hall.setDeadday(parseDate(request.getDeadday()));
        hall.setPlace(request.getPlace());
        hall.setPhone(request.getPhone());
    }

    // 프로필 변경
    private void updateProfileHall(Hall hall,String newProfileUrl) {
        // 기존 프로필 삭제
        if (hall.getProfile() != null && !hall.getProfile().isEmpty()) {
            try {
                String oldS3Key = fileUploadService.extractS3Key(hall.getProfile());
                fileUploadService.deleteFile(oldS3Key);
                log.info("기존 프로필 사진 삭제 완료: {}", oldS3Key);
            } catch (Exception e) {
                log.warn("기존 프로필 사진 삭제 실패: {}", e.getMessage());
            }
        }
        // 새 프로필 설정
        hall.setProfile(newProfileUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestListResponseDTO getRequests(Long hallId, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("추모관을 찾을 수 없습니다."));
        List<Request> requests =requestRepository.findByHallAndStatus(hall,RequestStatus.PENDING);
        return requestConverter.toRequestListResponseDTO(requests);
    }

    // "yyyy.MM.dd" -> LocalDate
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) { return null; }
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}