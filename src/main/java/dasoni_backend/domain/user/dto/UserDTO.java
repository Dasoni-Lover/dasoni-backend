package dasoni_backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserDTO {

    @Getter
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
    @NoArgsConstructor
    public static class LoginRequestDTO{
        @NotBlank
        private String logId;
        @NotBlank
        private String password;
    }

    @Getter
    @Builder
    public static class LoginResponseDTO {
        private String accessToken;
        private String refreshToken;
    }
    @Getter
    @NoArgsConstructor
    public static class RefreshTokenRequestDTO {
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        private String refreshToken;
    }

    @Getter
    @Builder
    public static class AccessTokenResponseDTO {
        private String accessToken;
    }
}
