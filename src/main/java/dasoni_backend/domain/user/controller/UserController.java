package dasoni_backend.domain.user.controller;

import dasoni_backend.domain.user.dto.UserDTO.CheckResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.ProfileRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.RefreshTokenRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.AccessTokenResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.LoginRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.LoginResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.RegisterRequestDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.user.service.UserService;
import dasoni_backend.global.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO request) {
        userService.register(request);
        return ResponseEntity.ok().build();
    }

    // 회원가입 중복 확인 (아이디 체크)
    @GetMapping("/register/check")
    public ResponseEntity<CheckResponseDTO> checkAvailable(@RequestParam("logid") String logId) {
        return ResponseEntity.ok(userService.checkAvailable(logId));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthUser User user) {
        userService.logout(user);
        return ResponseEntity.ok().build();
    }

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO request) {
        AccessTokenResponseDTO response = userService.refresh(request);
        return ResponseEntity.ok(response);
    }

    // 프로필 수정
    @PostMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileRequestDTO request, @AuthUser User user) {
        userService.updateProfile(request,user);
        return ResponseEntity.ok().build();
    }
}
