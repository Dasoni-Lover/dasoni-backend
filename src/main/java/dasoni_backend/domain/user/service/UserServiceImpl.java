package dasoni_backend.domain.user.service;

import dasoni_backend.domain.user.converter.UserConverter;
import dasoni_backend.domain.user.dto.UserDTO.LoginRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.LoginResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.RegisterRequestDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.user.repository.UserRepository;
import dasoni_backend.global.auth.JwtTokenProvider;
import dasoni_backend.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserConverter userConverter;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterRequestDTO request){
        User user = userConverter.RegisterToUser(request);
        userRepository.save(user);
    }
    @Override
    @Transactional
    public boolean checkDuplicate(String logId) {
        return userRepository.existsByLogId(logId);
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. logId로 User 조회
        User user = userRepository.findByLogId(request.getLogId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 3. JWT 토큰 생성 및 반환
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        long refreshTtlMs = jwtTokenProvider.getTokenRemainingTime(refreshToken);
        redisService.saveRefreshToken(user.getId().toString(), refreshToken, refreshTtlMs);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(User user){
        redisService.deleteRefreshToken(String.valueOf(user.getId()));
    }
}
