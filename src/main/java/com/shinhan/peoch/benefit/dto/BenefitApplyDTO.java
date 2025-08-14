package com.shinhan.peoch.benefit.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BenefitApplyDTO {
    private Long cardId;         // 내 카드 ID
    private List<Long> benefitIds; // 적용할 혜택 ID 목록
}
