package dasoni_backend.domain.hall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class HallDTO {

    // 1. 입장한 추모관 / 관리하는 추모관 목록 조회
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HallListResponseDTO {

        // 추모관 리스트
        private List<HallResponseDTO> halls;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class HallResponseDTO {

            private String profile;

            private String name;

            private LocalDateTime birthday;

            private LocalDateTime deadday;

            private String adminName;
        }

    }
}
