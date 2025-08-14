package com.shinhan.peoch.benefit.dto;

import com.shinhan.entity.MyBenefitStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MyBenefitDTO {
    private Long benefitId;    // 복합키 중 benefitId
    private String name;       // BenefitEntity의 name
    private String description; // BenefitEntity의 description
    private Integer fee;       // BenefitEntity의 fee
    private MyBenefitStatus status;  // 내 혜택의 상태
    private Integer usedCount;       // 사용 횟수
    private Float discountRate;

    // benefitId만 받는 생성자 (삭제 응답 전용)
    public MyBenefitDTO(Long benefitId) {
        this.benefitId = benefitId;
    }
}
