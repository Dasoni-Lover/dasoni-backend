package dasoni_backend.global.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String token = extractToken(request);

        // 2. 토큰이 있으면 검증
        if (token != null) {
            if(!jwtTokenProvider.validateToken(token)) {
            // 유효하지 않은 토큰이면 401 에러
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"유효하지 않은 토큰입니다.\"}");
            return;
            }
            // SecurityContext에 인증 정보 저장
            Long userId = jwtTokenProvider.getUserId(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,  // principal
                            null,    // credentials
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))  // authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 3. 토큰이 없거나 유효하면 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 토큰 추출
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 특정 경로는 토큰 검증 제외 (선택사항)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        System.out.println("🔍 Filter Check - Path: " + path);
        // 로그인, 회원가입 등은 토큰 없이도 접근 가능
        return path.equals("/api/users/register") ||
                path.equals("/api/users/register/check") ||
                path.equals("/api/users/login") ||
                path.equals("/api/users/refresh") ||
                path.equals("/api/files/images/presigned-url") ||
                path.startsWith("/api/share-links/");
    }
}