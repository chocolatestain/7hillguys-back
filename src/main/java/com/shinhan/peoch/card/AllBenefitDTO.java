package com.shinhan.peoch.card;

import com.shinhan.entity.BenefitEntity;
import lombok.Data;

@Data
public class AllBenefitDTO {
    // 혜택 엔티티
    private BenefitEntity benefit;
    // 상점명
    private String storeName;
    // 사용중 여부
    private boolean inUse;
    // 놓친 혜택 금액
    private Long missedBenefitAmount;

}
