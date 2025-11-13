package dasoni_backend.domain.hall.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dasoni_backend.global.enums.Personality;
import dasoni_backend.global.enums.RelationKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

        private Long hallId;

        private String profile;

        private String name;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDate birthday;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
        private LocalDate deadday;

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

    // 3. 본인/타인 추모관 개설 응답
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HallCreateResponseDTO {
        private Long hallId;
    }

    @Getter
    @Builder
    public static class MyHallResponseDTO {
        private boolean myHallExists;
        private Long hallId;
    }

    // 4. 타인 추모관 개설 요청
    @Getter
    @NoArgsConstructor
    public static class HallCreateRequestDTO {

        @NotBlank
        private String name;

        @NotNull
        // FRIEND, LOVER, FAMILY
        private RelationKind relation;

        @NotNull
        private String birthday;

        @NotNull
        private String deadday;

        @NotNull
        @Size(min = 3, max = 3)
        // 3개 고정
        private List<Personality> natures;

        @NotBlank
        private String review;

        private String profile;

        private String place;

        private String phone;

        // 사망확인서
        private String docs;
    }

    // 추모관 내용 조회
    @Getter
    @Builder
    public static class HallDetailDataResponseDTO {
        // follower, admin, me
        private String role;
        private HallDetailResponseDTO data;
    }

    @Getter
    @Builder
    public static class HallDetailResponseDTO {

        private String name;

        private String profile;

        private String birthday;

        private String deadday;

        private List<String> nature;

        private String place;

        private String phone;

        private String review;

        private String adminName;

        private boolean isOpen;
    }
}
