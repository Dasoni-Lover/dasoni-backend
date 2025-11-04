package dasoni_backend.global.config;

import dasoni_backend.domain.user.repository.UserRepository;
import dasoni_backend.domain.user.service.UserService;
import dasoni_backend.global.annotation.AuthUser;
import dasoni_backend.global.auth.JwtTokenProvider;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // @AuthUser 어노테이션이 있으면 이 Resolver 실행
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class);
    }

    // 실제로 User 객체를 조회해서 반환
    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) {

        HttpServletRequest request =
                (HttpServletRequest) webRequest.getNativeRequest();

        // 1. 토큰 추출
        String token = extractToken(request);

        if (token == null) {
            throw new IllegalArgumentException("인증 토큰이 없습니다.");
        }

        // 2. 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserId(token);

        // 3. DB에서 User 조회 후 반환
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    // Authorization 헤더에서 토큰 추출
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}