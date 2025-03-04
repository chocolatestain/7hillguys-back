package com.shinhan.peoch.auth.controller;

import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.auth.service.UserService;
import com.shinhan.peoch.security.jwt.JwtUtil;
import com.shinhan.peoch.security.jwt.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public String register(@RequestBody UserEntity user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        try {
            Map<String, String> tokens = userService.login(email, password);
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String refreshToken = request.get("refreshToken");

        try {
            String newAccessToken = userService.refreshAccessToken(email, refreshToken);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰입니다.");
        }

        token = token.substring(7); //"Bearer " 제거

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰입니다.");
        }

        long expirationTime = jwtUtil.getExpirationTime(token);
        tokenBlacklistService.blacklistToken(token, expirationTime);

        return ResponseEntity.ok("로그아웃이 성공적으로 완료되었습니다.");
    }

}
