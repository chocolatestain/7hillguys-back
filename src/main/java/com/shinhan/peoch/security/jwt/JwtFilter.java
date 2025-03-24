package com.shinhan.peoch.security.jwt;

import com.shinhan.peoch.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("=== [JwtFilter] 요청 도착: {} ===", request.getRequestURI());

        // 1 헤더 또는 쿠키에서 JWT 가져오기
        String token = resolveTokenFromHeader(request);
        if (token == null) {
            token = resolveTokenFromCookie(request);
        }

        if (token == null) {
            log.warn("[JwtFilter] JWT 없음. 요청을 필터링하지 않고 계속 진행.");
            filterChain.doFilter(request, response);
            return;
        }

//        log.info("[JwtFilter] 추출된 JWT: {}", token);

        // 2️⃣ 블랙리스트 확인
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            log.warn("[JwtFilter] 블랙리스트에 등록된 JWT 사용 감지. 접근 거부.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "블랙리스트에 등록된 토큰입니다.");
            return;
        }

        // 3️⃣ JWT 유효성 검사
        if (!jwtUtil.validationToken(token)) {
            log.warn("[JwtFilter] JWT가 유효하지 않음");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT입니다.");
            return;
        }

        // 4️⃣ 사용자 정보 추출
        String email = jwtUtil.getUserEmail(token);
        Long userId = jwtUtil.getUserId(token);

        if (email == null || userId == null) {
            log.warn("[JwtFilter] JWT에서 이메일 또는 userId 추출 실패");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT에서 사용자 정보를 추출할 수 없습니다.");
            return;
        }

//        log.info("[JwtFilter] 추출된 사용자 정보: 이메일={}, userId={}", email, userId);

        // 5️⃣ 사용자 인증 및 SecurityContext 업데이트
        UserDetails userDetails = userService.loadUserByUsername(email);
        if (userDetails == null) {
            log.warn("[JwtFilter] 이메일에 해당하는 사용자 정보 없음: {}", email);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "사용자 정보를 찾을 수 없습니다.");
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("userId", userId); // ✅ userId를 request에 저장
//        log.info("[JwtFilter] 사용자 인증 완료. SecurityContext 업데이트됨. 사용자: {}", email);

        filterChain.doFilter(request, response);
    }

    // ✅ Authorization 헤더에서 JWT 가져오기
    private String resolveTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // ✅ 쿠키에서 JWT 가져오기
    private String resolveTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // ✅ 로그인 및 회원가입 경로에서 필터링 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/public/") ||
                path.startsWith("/api/docs");
    }
}