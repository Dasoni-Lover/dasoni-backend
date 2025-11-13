package dasoni_backend.domain.hall.service;

import dasoni_backend.domain.hall.converter.HallConverter;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailDataResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
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
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.voice.dto.VoiceDTOs.VoiceDTO;
import dasoni_backend.domain.voice.entity.Voice;
import dasoni_backend.global.S3.service.FileUploadService;
import dasoni_backend.global.enums.Personality;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor // 자동 생성자 주입
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final HallQueryRepository hallQueryRepository;
    private final relationshipRepository relationshipRepository;
    private final relationshipNatureRepository relationshipNatureRepository;
    private final FileUploadService fileUploadService;

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
    public void uploadVoice(Long hallId, VoiceDTO request, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!user.equals(hall.getAdmin())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        Voice voice = Voice.builder()
                .url(request.getUrl())
                .updateAt(LocalDateTime.now())
                .build();

        hall.setVoice(voice);
        hallRepository.save(hall);
    }

    @Override
    @Transactional
    public void updateVoice(Long hallId, VoiceDTO request, User user){
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Hall not found"));

        if (!user.equals(hall.getAdmin())) {
            throw new IllegalStateException("권한이 없습니다");
        }

        // 기존 voice 삭제
        Voice oldVoice = hall.getVoice();
        if (oldVoice != null && oldVoice.getUrl() != null) {
            try {
                String oldS3Key = fileUploadService.extractS3Key(oldVoice.getUrl());
                fileUploadService.deleteFile(oldS3Key);
                log.info("기존 음성 파일 삭제 완료: {}", oldS3Key);
            } catch (Exception e) {
                log.warn("기존 음성 파일 삭제 실패: {}", e.getMessage());
            }
        }

        Voice voice = Voice.builder()
                .url(request.getUrl())
                .updateAt(LocalDateTime.now())
                .build();

        // 업데이트 후 저장
        hall.setVoice(voice);
        hallRepository.save(hall);
    }

}