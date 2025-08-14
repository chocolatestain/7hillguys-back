package com.shinhan.peoch.config;

import com.shinhan.peoch.auth.service.UserService;
import com.shinhan.peoch.security.jwt.JwtFilter;
import com.shinhan.peoch.security.jwt.JwtUtil;
import com.shinhan.peoch.security.jwt.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String[] USER_LIST = {"/api/review/**", "/api/investment/status", "/api/contract/**","/api/investment/**"};
    private static final String[] ADMIN_LIST ={};
    private static final String[] WHITE_LIST={
           "/api/user/**", "/api/auth/register", "/api/auth/login", "/api/review/save", "/api/review/file", "/api/auth/logout", "/api/usersearch/**"
    };

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> {
            // actuator/health 외에도 전체 actuator 허용(옵션)
            auth.requestMatchers("/actuator/**").permitAll();
            // 에러/정적
            auth.requestMatchers("/error", "/favicon.ico", "/static/**", "/css/**", "/js/**", "/images/**").permitAll();

            // 공개 엔드포인트만 나열
            auth.requestMatchers(
                "/api/auth/register",
                "/api/auth/login",
                "/api/auth/userId",    
                "/api/user/**",
                "/api/review/save",
                "/api/review/file",
                "/api/usersearch/**",
                "/actuator/**",
                "/api/actuator/**"
            ).permitAll();

            // 사용자/관리자 권한 경로
            auth.requestMatchers("/api/review/**", "/api/investment/status", "/api/contract/**", "/api/investment/**").hasRole("USER");
            auth.requestMatchers("/api/admin/**").hasRole("ADMIN");

            // 로그아웃은 인증 필요
            auth.requestMatchers("/api/auth/logout").authenticated();

            // 나머지는 인증 필요
            auth.anyRequest().authenticated();
        });

    // JwtFilter는 UsernamePasswordAuthenticationFilter 이전
    http.addFilterBefore(new JwtFilter(userService, jwtUtil, tokenBlacklistService),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();

    // allowCredentials(true) 사용 시 * 불가 → 정확/패턴 지정
    // 필요하면 둘 중 하나만 사용: setAllowedOrigins() 또는 setAllowedOriginPatterns()
    cfg.setAllowedOriginPatterns(Arrays.asList(
        "http://lvndr.kro.kr",
        "https://lvndr.kro.kr",
        "http://localhost:*",
        "http://192.168.0.172:*"
    ));

    cfg.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
    cfg.setAllowedHeaders(Arrays.asList("*"));
    cfg.setAllowCredentials(true);
    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
}

}
