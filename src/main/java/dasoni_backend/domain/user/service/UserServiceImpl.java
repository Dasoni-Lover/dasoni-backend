package dasoni_backend.domain.user.service;

import dasoni_backend.domain.user.converter.UserConverter;
import dasoni_backend.domain.user.dto.UserDTO.AccessTokenResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.CheckResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.LoginRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.LoginResponseDTO;
import dasoni_backend.domain.user.dto.UserDTO.ProfileRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.RefreshTokenRequestDTO;
import dasoni_backend.domain.user.dto.UserDTO.RegisterRequestDTO;
import dasoni_backend.domain.user.entity.User;
import dasoni_backend.domain.user.repository.UserRepository;
import dasoni_backend.global.S3.service.FileUploadService;
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
    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public void register(RegisterRequestDTO request){
        User user = userConverter.RegisterToUser(request);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public CheckResponseDTO checkAvailable(String logId) {
        return CheckResponseDTO.builder().isAvailable(!userRepository.existsByLogId(logId)).build();
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
    @Transactional(readOnly = true)
    public AccessTokenResponseDTO refresh(RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();

        // 1. Refresh Token 형식 검증 (JWT 자체 유효성)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. Refresh Token에서 userId 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // 3. Redis에 저장된 Refresh Token과 비교
        String savedRefreshToken = redisService.getRefreshToken(userId.toString());

        if (savedRefreshToken == null) {
            throw new IllegalArgumentException("로그아웃된 사용자입니다.");
        }

        if (!savedRefreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 4. User 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        // 5. 새로운 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);

        return AccessTokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .build();
    }
    @Override
    @Transactional
    public void logout(User user){
        redisService.deleteRefreshToken(String.valueOf(user.getId()));
    }

    @Override
    @Transactional
    public void updateProfile(ProfileRequestDTO request, User user) {
        if (user.getMyProfile() != null && !user.getMyProfile().isEmpty()) {
            String oldS3Key = fileUploadService.extractS3Key(user.getMyProfile());
            fileUploadService.deleteFile(oldS3Key);
        }
        user.setMyProfile(request.getProfile());
        userRepository.save(user);
    }
}
