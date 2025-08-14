package com.shinhan.peoch.card;

import com.shinhan.entity.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CardStatementDTO {
    // 금액
    private Integer amount;
    // 결제일
    private LocalDateTime paymentDate;
    // 결제 방법
    private PaymentStatus paymentStatus;
    // 상점 이름
    private String storeName;
    // 할부
    private Integer installmentMonth;
    // 할부 회차
    private Integer installmentRound;
    // 혜택 할인 금액
    private Integer benefitDiscountAmount;

}

