package dasoni_backend.domain.hall.converter;

import dasoni_backend.domain.hall.dto.HallDTO.HallCreateRequestDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallCreateResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.dto.HallDTO.HallResponseDTO;
import dasoni_backend.domain.hall.entity.Hall;
import dasoni_backend.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class HallConverter {

    public static HallResponseDTO toHallResponseDTO(Hall hall) {
        if(hall == null) return null;

        return HallResponseDTO.builder()
                .profile(hall.getProfile())
                .name(hall.getName())
                .birthday(hall.getBirthday())
                .deadday(hall.getDeadday())
                .adminName(hall.getAdmin().getName())
                .build();
    }

    public static HallListResponseDTO toHallListResponseDTO(List<Hall> halls) {
        if (halls == null) return HallListResponseDTO.builder().halls(null).build();

        return HallListResponseDTO.builder()
                .halls(halls.stream()
                        .map(HallConverter::toHallResponseDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    //  본인/타인 추모관 개설(hallId 반환)
    public static HallCreateResponseDTO toHallCreateResponseDTO(Hall hall) {

        return HallCreateResponseDTO.builder()
                .hallId(hall.getId())
                .build();
    }

    // 본인 추모관 필드 채우기
    public static Hall fromSaveRequest(User user) {

        // 따로 요청 없이, User의 정보를 바탕으로 엔티티 생성
        Hall hall = Hall.builder()
                .admin(user)
                .subjectId(user.getId())
                .name(user.getName())
                .birthday(user.getBirthday())
                .profile(user.getMyProfile())
                .createdAt(LocalDateTime.now())
                .isOpened(true)
                .userNum(1)
                .build();

        return hall;
    }

    // 타인 추모관 필드 채우기
    public static Hall fromSaveRequestForOther(User admin, HallCreateRequestDTO request) {
        Hall hall = Hall.builder()
                .admin(admin)
                .name(request.getName())
                .targetNatures(request.getNatures())
                .review(request.getReview())
                .birthday(request.getBirthday())
                .deadday(request.getDeadday())
                .profile(request.getProfile())
                .place(request.getPlace())
                .phone(request.getPhone())
                .docs(request.getDocs())
                .createdAt(LocalDateTime.now())
                .isOpened(true)
                .userNum(1)
                .build();
        return hall;
    }
}
