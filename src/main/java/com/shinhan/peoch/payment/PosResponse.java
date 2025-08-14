package com.shinhan.peoch.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PosResponse {
    // 성공 여부
    private boolean success;

    // 응답 메시지 = "결제가 승인되었습니다.", "유효하지 않은 카드입니다."
    // "할부 결제에 실패했습니다.", "유효하지 않은 상점입니다."
    private String message;

    // "PAYMENT_APPROVED" = 결제 승인, "NOT_VALID_CARD" = 유효하지 않은 카드
    // "LIMIT_EXCEEDED" = 한도 초과, "NOT_VALID_STORE" = 유효하지 않은 상점
    // "INSTALLMENT_FAILED" = 할부 결제 실패
    // "REFUND_APPROVED" = 환불 승인, "REFUND_DECLINED" = 환불 거절,
    private String code;

}
