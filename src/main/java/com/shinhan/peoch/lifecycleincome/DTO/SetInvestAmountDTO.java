package com.shinhan.peoch.lifecycleincome.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetInvestAmountDTO {
    private Long maxInvestment;         // 최대 지원 가능 금액 (원)
    private String expectedIncomes;  // 예상소득 리스트
//    private Double inflationAdjustedRate; // 물가상승률 적용된 환율
    private String inflationRate;         // JSON 형식 물가상승률 데이터

}
