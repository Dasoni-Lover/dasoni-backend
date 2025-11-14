package dasoni_backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class UserDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RegisterRequestDTO {
        @NotBlank
        private String name;
        @NotBlank
        private Boolean gender;
        @NotBlank
        private String birthday;  // "2002.04.08" 형태
        @NotBlank
        private String logId;
        @NotBlank
        private String password;
        private String myProfile;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LoginRequestDTO{
        @NotBlank
        private String logId;
        @NotBlank
        private String password;
    }

    @Getter
    @Setter
    @Builder
    public static class LoginResponseDTO {
        private String accessToken;
        private String refreshToken;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    public static class RefreshTokenRequestDTO {
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        private String refreshToken;
    }

    @Getter
    @Setter
    @Builder
    public static class AccessTokenResponseDTO {
        private String accessToken;
    }

    @Getter
    @Setter
    @Builder
    public static class CheckResponseDTO {
        private Boolean isAvailable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitorListResponseDTO {
        private Integer visitorCount;
        private List<VisitorResponseDTO> visitors;
    }

    // 개별 방문자 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitorResponseDTO {
        private Long userId;
        private String name;
        private String relation;  // "가족", "친구", "연인"
        private List<String> natures;  // ["따뜻한", "성실한", "착한"]
        private String review;
        private String detail;
    }
}
