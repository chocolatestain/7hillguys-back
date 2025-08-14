package com.shinhan.peoch.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
 
import com.shinhan.peoch.auth.dto.UserResponseDTO;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.auth.service.UserService;
import com.shinhan.peoch.security.SecurityUser;
import com.shinhan.peoch.security.jwt.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import com.shinhan.peoch.auth.dto.UserNameResponseDTO;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    /**
     * 회원가입 API
     */
    @PostMapping("/register")
    public String register(@RequestBody UserEntity user) {
        return userService.register(user);
    }

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserEntity user) {
        return authService.login(user);
    }

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("로그아웃이 성공적으로 완료되었습니다.");
    }

    /**
     * 현재 사용자 정보 조회 API
     */
    @GetMapping("/user")
    public UserResponseDTO getCurrentUser(@AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        return new UserResponseDTO(securityUser.getUserId());
    }
    @GetMapping("/userId")
    public UserNameResponseDTO getCurrentUserName(@AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        return new UserNameResponseDTO(securityUser.getName());
    }
}
 
