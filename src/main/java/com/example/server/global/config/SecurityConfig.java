package com.example.server.global.config;

import com.example.server.global.security.JwtAccessDeniedHandler;
import com.example.server.global.security.JwtAuthenticationEntryPoint;
import com.example.server.global.security.JwtAuthenticationFilter;
import com.example.server.global.security.JwtProperties;
import com.example.server.global.security.JwtProvider;
import com.example.server.global.security.RefreshCookieProperties;
import com.example.server.global.security.TokenBlacklist;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 토큰은 Authorization 헤더로만 받으므로(C-07) 세션과 CSRF를 사용하지 않는다.
 * 쿠키로 토큰을 주고받게 바뀌면 CSRF 설정을 다시 검토해야 한다.
 * CORS는 여기 한 곳에서만 설정한다. WebMvcConfigurer에만 설정하면 사전 요청(preflight)이 401로 막힌다.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, RefreshCookieProperties.class, CorsProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    // API 명세에서 인증이 필요 없는(X) 엔드포인트
    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    };

    private final JwtProvider jwtProvider;
    private final TokenBlacklist tokenBlacklist;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                        // 서버 내부 오류 처리 경로. 막으면 실제 오류가 401로 가려진다.
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, tokenBlacklist), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 프론트엔드(다른 포트)에서 오는 브라우저 요청을 허용한다.
     * allowCredentials = true: 쿠키를 주고받는 방식(C-07)으로 바뀌어도 동작하도록 열어 둔다.
     * 이 설정에서는 출처에 와일드카드(*)를 쓸 수 없어 허용 주소를 정확히 지정한다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        // 사전 요청 결과를 브라우저가 1시간 동안 재사용한다. (요청 수 감소)
        configuration.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
