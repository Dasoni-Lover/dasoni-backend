package dasoni_backend.domain.hall.converter;

import dasoni_backend.domain.hall.dto.HallDTO.HallListResponseDTO;
import dasoni_backend.domain.hall.entity.Hall;

import java.util.List;
import java.util.stream.Collectors;

public class HallConverter {

    public static HallListResponseDTO.HallResponseDTO toHallResponseDTO(Hall hall) {
        if(hall == null) return null;

        return HallListResponseDTO.HallResponseDTO.builder()
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
}
