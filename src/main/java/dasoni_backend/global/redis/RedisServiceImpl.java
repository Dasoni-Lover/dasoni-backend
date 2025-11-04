package dasoni_backend.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService{
    private final RedisTemplate<String, String> redisTemplate;

    // Refresh Token 저장
    @Override
    public void saveRefreshToken(String userId, String refreshToken, Long expiration) {
        redisTemplate.opsForValue().set(
                "RT:" + userId,  // Key: RT:userId
                refreshToken,
                expiration,
                TimeUnit.MILLISECONDS
        );
    }

    // Refresh Token 조회
    @Override
    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get("RT:" + userId);
    }

    // Refresh Token 삭제
    @Override
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("RT:" + userId);
    }
}
