package com.shinhan.peoch.security.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shinhan.peoch.auth.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        // 1. 헤더 또는 쿠키에서 JWT 가져오기
        String token = resolveTokenFromHeader(request);
        if (token == null) {
            token = resolveTokenFromCookie(request);
        }

        if (token == null) {
            log.warn("[JwtFilter] JWT 없음. 요청을 필터링하지 않고 계속 진행.");
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 블랙리스트 확인
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            log.warn("[JwtFilter] 블랙리스트에 등록된 JWT 사용 감지. 접근 거부.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "블랙리스트에 등록된 토큰입니다.");
            return;
        }

        // 3. JWT 검증
        if (!jwtUtil.validationToken(token)) {
            log.warn("[JwtFilter] JWT가 유효하지 않습니다! token: {}", token);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT입니다.");
            return;
        }

        // 4. JWT에서 사용자 정보 추출
        String email = jwtUtil.getUserEmail(token);
        Long userId = jwtUtil.getUserId(token);
        log.info("[JwtFilter] JWT에서 추출한 email: {}, userId: {}", email, userId);

        if (email == null || userId == null) {
            log.warn("[JwtFilter] JWT에서 이메일 또는 userId 추출 실패");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT에서 사용자 정보를 추출할 수 없습니다.");
            return;
        }

        // 5. UserDetails 로드 후 인증 객체 생성
        UserDetails userDetails = userService.loadUserByUsername(email);
        log.info("[JwtFilter] 로드된 UserDetails 객체: {}", userDetails);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("[JwtFilter] SecurityContext에 인증 객체 저장 완료: {}", authentication.getPrincipal());
        
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 JWT 가져오기
    private String resolveTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 쿠키에서 JWT 가져오기 (디버깅 로그 추가)
    private String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("[JwtFilter] 발견된 쿠키: {} = {}", cookie.getName(), cookie.getValue());
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        } else {
            log.info("[JwtFilter] 요청에 쿠키가 존재하지 않습니다.");
        }
        return null;
    }

    // 로그인, 회원가입 등 인증 없이 접근 가능한 경로는 필터 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/public/") ||
               path.startsWith("/api/docs");
    }
}
