package com.shinhan.peoch.benefit.dto;

import com.shinhan.entity.BenefitEntity;
import com.shinhan.entity.CardEntity;
import com.shinhan.entity.MyBenefitEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BenefitResponseDTO {
    // 내 카드 정보 (예: CardEntity 혹은 CardResponseDTO)
    private CardEntity card;
    // 기존에 적용된 혜택
    private List<MyBenefitEntity> appliedBenefits;
    // 사용 가능한 혜택
    private List<BenefitEntity> availableBenefits;

}
