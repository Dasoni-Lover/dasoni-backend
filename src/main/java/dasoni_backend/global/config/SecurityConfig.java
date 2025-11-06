package dasoni_backend.global.config;

import dasoni_backend.global.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용 시)
            .csrf(AbstractHttpConfigurer::disable)
            // 세션 사용 안 함
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 권한 설정
            .cors(c -> {})
                .authorizeHttpRequests(auth -> auth
                    // OPTIONS 요청 먼저
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // 회원가입 - 명시적으로 모든 하위 경로 포함
                    .requestMatchers("/api/users/register", "/api/users/register/**").permitAll()
                    .requestMatchers("/api/users/login", "/api/users/login/**").permitAll()
                    .requestMatchers("/api/users/refresh", "/api/users/refresh/**").permitAll()

                    .anyRequest().authenticated()
                )
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}