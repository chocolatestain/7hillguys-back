package com.shinhan.peoch.lifecycleincome.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReallyExitResponseDTO {
     private String startDate; // 투자 시작일 (YYYY-MM-DD)
     private String endDate;   // 투자 종료일 (YYYY-MM-DD)
     private List<MonthlyPaymentDTO> monthlyPayments; // 월별 결제 금액 리스트
     private Long totalAmount; // 총 투자 금액 합계
     private Long adjustedAmount; // 물가 상승률 고려 환급 금액
}
