package dasoni_backend.domain.hall.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    public static class HallListResponseDTO {

        // 추모관 리스트
        private List<HallResponseDTO> halls;
    }
    @Getter
    @Builder
    public static class HallResponseDTO {

        private String profile;

        private String name;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDateTime birthday;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDateTime deadday;

        private String adminName;
    }

    
    // 2. 사이드바 정보
    @Getter
    @Builder
    public static class SidebarResponseDTO {

        private String name;

        // 프로필 사진
        private String myProfile;

        private Integer notiCount; // 알림 수, 추후에 연동
    }

    // 3. 본인 추모관 개설
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HallCreateResponseDTO {

        private Long hallId;
    }
}
