package dasoni_backend.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService{
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    // Refresh Token 저장
    @Override
    public void saveRefreshToken(String userId, String refreshToken, Long expiration) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,  // Key: RT:userId
                refreshToken,
                expiration,
                TimeUnit.MILLISECONDS
        );
    }
    // Refresh Token 조회
    @Override
    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    // Refresh Token 삭제
    @Override
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }
}
