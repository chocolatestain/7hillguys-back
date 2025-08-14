package com.shinhan.peoch.lifecycleincome.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor

public class ExitResponseDTO {
     String firstExpectedIncome; // JSON 가입 당시 예상치
     String lastExpectedIncome; // JSON 마지막 예상치
     long discountAmount; //할인 된 가격 총액
     long investValue;//사용한 금액
     long exitCost; //계약 종료 비용
     String StartDate;//계약서 시작일
     String EndDate;//계약서 종료일
     String message; //응답 메시지
}
