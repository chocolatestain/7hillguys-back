package com.shinhan.peoch.benefit.controller;


import com.shinhan.entity.MyBenefitEntity;
import com.shinhan.entity.PaymentEntity;
import com.shinhan.peoch.benefit.dto.BenefitApplyDTO;
import com.shinhan.peoch.benefit.dto.BenefitResponseDTO;
import com.shinhan.peoch.benefit.dto.MyBenefitDTO;
import com.shinhan.peoch.benefit.service.MyBenefitService;
import com.shinhan.peoch.payment.PaymentService;
import com.shinhan.peoch.security.jwt.JwtTokenProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/benefit")
@RequiredArgsConstructor
@Getter
@Slf4j
public class MyBenefitController {

    private final MyBenefitService myBenefitService;
    private final JwtTokenProvider jwtTokenProvider;

    // 1. 내 카드에 적용된 혜택 및 사용 가능한 혜택 목록 가져오기
    @GetMapping("/card")
    public ResponseEntity<?> getCardBenefits( @CookieValue(value = "jwt", required = false) String jwtToken) {
        // 예시: 토큰에서 사용자 ID 추출 (실제 구현은 JWT 라이브러리 등을 사용)
        // String token = authHeader.substring(7); // "Bearer " 제거
        // Long userId = tokenService.getUserIdFromToken(token);
        // 여기서는 임시로 고정값 사용 예: userId = 16L;
        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("🚨 [ContractController] JWT 쿠키 없음!");
            return ResponseEntity.status(401).body(Map.of("error", "인증 토큰이 없습니다."));
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);


        BenefitResponseDTO response = myBenefitService.getBenefitsByUserId(userId);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{benefitId}")
    public ResponseEntity<MyBenefitDTO> deleteBenefit(
            @PathVariable("benefitId") Long benefitId,
            @RequestParam("cardId") Long cardId) {
        MyBenefitDTO deletedDto = myBenefitService.deleteBenefit(benefitId, cardId);
        return ResponseEntity.ok(deletedDto);
    }

    @PostMapping("/apply")
    public ResponseEntity<List<MyBenefitDTO>> applyBenefits(@RequestBody BenefitApplyDTO benefitApplyDTO) {

        log.info("benefitApplyDTO: {}", benefitApplyDTO);

        List<MyBenefitDTO>  mybenefit = myBenefitService.applyBenefits(benefitApplyDTO);
        return ResponseEntity.ok(mybenefit);
    }

    // 전체 결제 내역 조회 (예: GET /payments?month=2025-03)
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentEntity>> getPaymentsByCardAndMonth(
            @RequestParam("cardId") Long cardId,
            @RequestParam("month") String month) {
        List<PaymentEntity> payments = myBenefitService.getPaymentsByCardAndMonth(cardId, month);
        return ResponseEntity.ok(payments);
    }
}
