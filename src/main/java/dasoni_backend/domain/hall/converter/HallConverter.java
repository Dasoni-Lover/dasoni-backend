package dasoni_backend.domain.hall.converter;

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

    //  본인 추모관 개설(hallId 반환)
    public static HallCreateResponseDTO toHallCreateResponseDTO(Hall hall) {

        return HallCreateResponseDTO.builder()
                .hallId(hall.getId())
                .build();
    }

    // 본인 추모관 필드 채우기
    public static Hall fromSaveRequest(User user, LocalDateTime now) {

        Hall hall = new Hall();
        hall.setAdmin(user);
        hall.setName(user.getName());
        hall.setBirthday(user.getBirthday());
        hall.setProfile(user.getMyProfile());
        hall.setCreatedAt(now);
        hall.setUserNum(1);
        hall.setIsOpened(true);
        return hall;
    }
}
