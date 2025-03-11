package com.shinhan.peoch.config;

import com.shinhan.peoch.security.jwt.JwtFilter;
import com.shinhan.peoch.security.jwt.JwtUtil;
import com.shinhan.peoch.security.jwt.TokenBlacklistService;
import com.shinhan.peoch.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String[] USER_LIST = {"/api/review/**", "/api/investment/status", "/api/contract/**"};
    private static final String[] ADMIN_LIST ={};
    private static final String[] WHITE_LIST={
            "/api/auth/register", "/api/auth/login", "/api/review/save", "/api/review/file", "/api/auth/logout"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    // 인증 없이 접근 가능한 엔드포인트
                    auth.requestMatchers(WHITE_LIST).permitAll();
                    // 일반 사용자 전용 엔드포인트
                    auth.requestMatchers(USER_LIST).hasRole("USER");
                    // 관리자 전용 엔드포인트
                    auth.requestMatchers(ADMIN_LIST).hasRole("ADMIN");
                    // 로그아웃은 인증된 사용자만 접근 가능
                    auth.requestMatchers("/api/auth/logout").authenticated();
                    // 그 외의 모든 요청은 인증 필요
                    auth.anyRequest().authenticated();
                });

        // JwtFilter를 UsernamePasswordAuthenticationFilter 전에 추가
        httpSecurity.addFilterBefore(new JwtFilter(userService, jwtUtil, tokenBlacklistService),
                UsernamePasswordAuthenticationFilter.class);
//        httpSecurity.authorizeHttpRequests(auth -> {
//            auth.anyRequest().permitAll();
//        });
        return httpSecurity.build();
    }

    // CORS 설정 Bean: 필요에 따라 허용 도메인 및 메서드를 조정하세요.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // 신뢰할 수 있는 도메인만 허용
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true); // 쿠키, 인증 헤더 등을 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
