package dasoni_backend.global.redis;

public interface RedisService {
    void saveRefreshToken(String userId, String refreshToken, Long expiration);
    String getRefreshToken(String userId);
    void deleteRefreshToken(String userId);
}
