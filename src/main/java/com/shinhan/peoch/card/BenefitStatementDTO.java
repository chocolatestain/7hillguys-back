package com.shinhan.peoch.card;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BenefitStatementDTO {
    // 카드번호
    private String cardNumber;
    // 결제일
    private LocalDateTime paymentDate;
    // 상점 이름
    private String storeName;
    // 결제액
    private Integer originalAmount;
    // 할인된 결제액
    private Integer finalAmount;
    // 할인 금액
    private Integer benefitDiscountAmount;

}
