package com.shinhan.peoch.payment;

import com.shinhan.peoch.card.BenefitStatementResponseDTO;
import com.shinhan.peoch.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    // 결제 요청
    @PostMapping("/paymentRequest")
    public PosResponse processPayment(@RequestBody PosPaymentRequest request) {
        return paymentService.processPayment(request);
    }

    // 환불 요청
    @PostMapping("/refundRequest")
    public PosResponse processRefund(@RequestBody PosRefundRequest request) {
        return paymentService.processRefund(request);
    }

    // 결제 요청 더미 데이터
    @PostMapping("/bulkPaymentRequest")
    public ResponseEntity<List<PosResponse>> processBulkPayment(@RequestBody List<PosPaymentRequest> requests) {
        List<PosResponse> responses = requests.stream()
                .map(paymentService::processPayment) // 단건 결제 요청을 반복 처리
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // 전체 기간 혜택 조회 요청
    @GetMapping("/totalBenefit")
    public BenefitStatementResponseDTO getTotalBenefit(@CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }

        // JWT에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userId == null) {
            return null;
        }
        System.out.println(paymentService.getTotalBenefit(userId));
        return paymentService.getTotalBenefit(userId);
    }

}
