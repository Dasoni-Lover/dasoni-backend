package dasoni_backend.domain.user.service;

import dasoni_backend.domain.user.converter.UserConverter;
import dasoni_backend.domain.user.dto.UserDTO.LoginRequestDTO;
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
    public void login(String username, String password){

    }
    @Override
    @Transactional
    public void logout(LoginRequestDTO request){

        boolean matches = passwordEncoder.matches(
                request.getPassword(),  // 평문
                user.getPassword()      // 암호문
        );

    }
}
