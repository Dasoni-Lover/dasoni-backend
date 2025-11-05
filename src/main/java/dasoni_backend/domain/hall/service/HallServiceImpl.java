package dasoni_backend.domain.hall.service;

import dasoni_backend.domain.hall.converter.HallConverter;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallDetailDataResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.SidebarResponseDTO;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.hall.repository.HallQueryRepository;
import dasoni_backend.domain.hall.repository.HallRepository;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor // 자동 생성자 주입
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final UserRepository userRepository;
    private final HallQueryRepository hallQueryRepository;

    @Transactional(readOnly = true)
    @Override
    public HallListResponseDTO getHomeHallList(Long userId) {
        // 로그인하지 않았을 경우, 빈 리스트 반환(수정 불가능)
        if (userId == null)
            return HallConverter.toHallListResponseDTO(List.of());

        List<Hall> halls = hallRepository.findAllByFollowerUserIdOrderByCreatedAtDesc(userId);
        return HallConverter.toHallListResponseDTO(halls);
    }

    @Transactional(readOnly = true)
    @Override
    public HallListResponseDTO getManageHallList(Long adminId) {
        // 관리자 ID가 없을 경우(로그인 x), 빈 리스트 반환(수정 불가능)
        if (adminId == null)
            return HallConverter.toHallListResponseDTO(List.of());

        List<Hall> halls = hallRepository.findAllByAdminIdOrderByCreatedAtDesc(adminId);
        return HallConverter.toHallListResponseDTO(halls);
    }

    @Transactional(readOnly = true)
    @Override
    public SidebarResponseDTO getSidebar(Long userId) {
        // 로그인하지 않았을 경우
        if (userId == null) {
            return SidebarResponseDTO.builder().name(null).myProfile(null).notiCount(0).build();
        }

        // NPE 발생 방지
        Optional<User> opt = userRepository.findById(userId);
        // DB에 사용자 정보가 없는 경우
        if (opt.isEmpty()) {
            return SidebarResponseDTO.builder().name(null).myProfile(null).notiCount(0).build();
        }

        // Optional 안에 실제 User 엔티티를 꺼냄
        User user = opt.get();
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
        return hallRepository.findByAdminId(user.getId())
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

        return HallCreateResponseDTO.builder()
                .hallId(hall.getId())
                .build();
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
        if (userId.equals(subjectId)) {
            role = "me";
        } else if (userId.equals(adminId)) {
            role = "admin";
        } else {
            // 베타데모데이 이후로 승인된 사용자면 follower가 될 수 있게 로직 변경
            role = "follower";
        }

        // 관리자 리뷰만 추모관에 전시
        String adminReview = hall.getReview();

        // 상위 4개 natures 전시
        List<String> top4Natures = hallQueryRepository.findTop4NatureNames(hallId);

        // 변환(me일때는 필요없는 null처리 & 응답에서 숨김처리)
        return HallConverter.toHallDetailResponse(hall, role, adminReview, top4Natures);
    }
}
