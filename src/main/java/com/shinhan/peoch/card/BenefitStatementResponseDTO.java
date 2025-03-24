package com.shinhan.peoch.card;

import lombok.Data;

import java.util.List;

@Data
public class BenefitStatementResponseDTO {
    // 혜택 명세서
    private List<BenefitStatementDTO> statementList;
    // 이번 달 총 혜택 할인 금액
    private Integer totalBenefitDiscount;
    // 사용자 이름
    private String userName;

    // 전체 기간 총 혜택 할인 금액
    private Integer totalBenefit;

}

