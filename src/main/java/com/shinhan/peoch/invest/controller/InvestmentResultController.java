package com.shinhan.peoch.invest.controller;

import com.shinhan.peoch.invest.service.InvestmentResultService;
import com.shinhan.peoch.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/investment")
@RequiredArgsConstructor
public class InvestmentResultController {
    private final InvestmentResultService investmentResultService;
    private final JwtUtil jwtUtil;

    @GetMapping("/status")
    public ResponseEntity<String> checkInvestmentStatus(@CookieValue(value = "jwt", required = false) String jwtToken) {
        log.info("🔍 [백엔드] API 요청 수신: /api/investment/status");

        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("[백엔드] JWT 쿠키가 없습니다.");
            return ResponseEntity.status(401).body("토큰이 없습니다.");
        }

        Claims claims;
        try {
            // JWT 검증 및 Claims 추출
            claims = jwtUtil.parseClaims(jwtToken);
            log.info("[백엔드] 토큰 검증 성공");
        } catch (Exception e) {
            log.error("[백엔드] JWT 검증 실패", e);
            return ResponseEntity.status(401).body("잘못된 토큰입니다.");
        }

        Integer userId = claims.get("userId", Integer.class);
        log.info("[백엔드] 추출된 userId: {}", userId);

        if (userId == null) {
            log.warn("[백엔드] JWT에서 userId 추출 실패!");
            return ResponseEntity.status(401).body("JWT에서 userId를 찾을 수 없습니다.");
        }

        String status = investmentResultService.getInvestmentStatus(userId);
        log.info("[백엔드] 투자 상태 반환: {}", status);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approveInvestment(@CookieValue(name = "jwt", required = false) String jwtToken) {
        log.info("[백엔드] 투자 승인 요청 수신: /api/investment/approve");

        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("[백엔드] JWT 쿠키가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT 쿠키가 없습니다.");
        }

        Long userId = jwtUtil.getUserId(jwtToken);
        if (userId == null) {
            log.warn("[백엔드] JWT에서 사용자 Id를 추출하지 못했습니다.");
            return ResponseEntity.status(401).body("잘못된 토큰입니다.");
        }

        try {
            investmentResultService.approveInvestmentByUser(userId);
            log.info("[백엔드] 사용자({})의 투자 상태를 '승인'으로 업데이트 완료", userId);
            return ResponseEntity.ok("투자가 승인되었습니다.");
        } catch (Exception e) {
            log.error("[백엔드] 투자 승인 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).body("서버 오류로 승인 처리 실패.");
        }
    }

    @PostMapping("/reject")
    public ResponseEntity<?> rejectInvestment(@CookieValue(name = "jwt", required = false) String jwtToken) {
        log.info("[백엔드] 투자 거절 요청 수신: /api/investment/reject");

        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("[백엔드] JWT 쿠키가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT 쿠키가 없습니다.");
        }

        Long userId = jwtUtil.getUserId(jwtToken);
        if (userId == null) {
            log.warn("[백엔드] JWT에서 사용자 Id를 추출하지 못했습니다.");
            return ResponseEntity.status(401).body("잘못된 토큰입니다.");
        }

        try {
            investmentResultService.rejectInvestmentByUser(userId);
            log.info("[백엔드] 사용자({})의 투자 상태를 '거절'으로 업데이트 완료", userId);
            return ResponseEntity.ok("투자가 거절되었습니다.");
        } catch (Exception e) {
            log.error("[백엔드] 투자 승인 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).body("서버 오류로 승인 처리 실패.");
        }
    }
}
