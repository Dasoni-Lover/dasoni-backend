package dasoni_backend.global.redis;

public interface RedisService {
    public void saveRefreshToken(String userId, String refreshToken, Long expiration);
    public String getRefreshToken(String userId);
    public void deleteRefreshToken(String userId);
}
