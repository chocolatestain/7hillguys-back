package com.shinhan.peoch.security.jwt;

import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.exception.CustomException;
import com.shinhan.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 로그인 메서드
     */
    public ResponseEntity<Map<String, String>> login(UserEntity dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();

        // 1. 사용자 존재 여부 확인
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("해당 이메일의 사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        // 2. 비밀번호 검증(암호화된 password를 디코딩한 값과 입력한 패스워드 값이 다르면 null 반환)
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        // 3. JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user);

        // 4. HTTPOnly 쿠키 설정
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", accessToken)
                .httpOnly(true)                 // JavaScript에서 접근 불가 (XSS 보호)
                .secure(true)                   // HTTPS 환경에서만 전송
                .path("/")                      // 모든 경로에서 사용 가능
                .maxAge(Duration.ofDays(7))     // 쿠키 유효 기간 설정
                .build();

        // 5. 응답 반환
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())  // 쿠키로 반환
                .body(Map.of("message", "로그인 성공"));
    }

    /**
     * 로그아웃 메서드
     */
    public void logout(HttpServletRequest request) {
        String token = extractJwtFromCookie(request);
        String email = jwtUtil.getUserEmail(token);

        if (token == null) {
            throw new CustomException("JWT 토큰이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        if (!jwtUtil.validationToken(token)) {
            throw new CustomException("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
        }

        if (email == null) {
            throw new CustomException("JWT에서 이메일을 추출할 수 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        long expirationTime = jwtUtil.getExpirationTime(token);
        tokenBlacklistService.blacklistToken(token, expirationTime);
    }

    /**
     * 쿠키에서 JWT 토큰을 가져오는 메서드
     */
    private String extractJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) { // JWT 쿠키 이름 확인 (프론트에서 보낸 쿠키 이름과 맞춰야 함)
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
