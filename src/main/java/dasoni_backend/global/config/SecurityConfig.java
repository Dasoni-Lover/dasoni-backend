package dasoni_backend.global.config;

import dasoni_backend.global.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 너의 커스텀 필터

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 세션/CSRF/CORS 기본
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리
                .exceptionHandling(e -> e.authenticationEntryPoint(new Http403ForbiddenEntryPoint()))

                // 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // ✅ CORS 프리플라이트 무조건 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ 헬스/에러/루트
                        .requestMatchers("/", "/error", "/actuator/health").permitAll()

                        // ✅ 회원가입/로그인/토큰재발급(네가 추가한 경로들)
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/register/**",
                                "/api/users/login",
                                "/api/users/login/**",
                                "/api/halls/healthy"
                        ).permitAll()

                        // ✅ 파일 업로드용 presigned-url (공개 필요시)
                        .requestMatchers("/api/files/images/presigned-url").permitAll()

                        // ✅ 내부 연동용 FastAPI 엔드포인트(필요 시만 개방)
                        .requestMatchers(HttpMethod.POST, "/api/halls/photos/ai").permitAll()

                        // 🔒 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // 기본 폼로그인/HTTP Basic 비활성(원치 않으면 제거)
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())

                // JWT 필터 연결 (인증 전에 동작)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 전역 설정 (필요 시 도메인 제한)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*")); // 운영에선 구체적 도메인으로 제한 권장
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}