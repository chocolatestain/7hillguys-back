package com.shinhan.peoch.config;

import com.shinhan.peoch.security.jwt.JwtFilter;
import com.shinhan.peoch.security.jwt.JwtUtil;
import com.shinhan.peoch.security.jwt.TokenBlacklistService;
import com.shinhan.peoch.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String[] USER_LIST = {"/api/auth/logout"};
    private static final String[] ADMIN_LIST ={};
    private static final String[] WHITE_LIST={"/api/auth/register", "/api/auth/login"};

    @Bean
    public SecurityFilterChain filterChain2(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(c -> c.disable());

        httpSecurity.authorizeHttpRequests(auth -> {
            auth.requestMatchers(WHITE_LIST).permitAll();
            auth.requestMatchers(ADMIN_LIST).hasRole("ADMIN");
            auth.requestMatchers(USER_LIST).hasRole("USER");

            //나머지는 반드시 로그인하고 접근가능
            auth.anyRequest().authenticated();

        });

        httpSecurity.addFilterBefore(new JwtFilter(userService, jwtUtil, tokenBlacklistService),
                UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
