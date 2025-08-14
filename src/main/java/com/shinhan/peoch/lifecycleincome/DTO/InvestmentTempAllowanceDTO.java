package com.shinhan.peoch.lifecycleincome.DTO;

import com.shinhan.entity.ExpectedIncomeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
/**
 다른 파트가 안끝나서 대기
생애주기 소득말고 투자 받기로 한 금액 & 실제 사용한 금액 월별로
이번달까지 투자 받기로 한 금액(투자 가능한 금액 * 진행률)
진행률
투자 받은 금액(카드 사용액의 합)


그래프
월 받기로 한 금액(이번달만 표시)(임시한도가 있을시 임시 한도로 설정)
실제 사용한 금액
 */

@Data
@Builder
@AllArgsConstructor
public class InvestmentTempAllowanceDTO {


     long availableAmount; // 현재 지원 가능 금액
     long investValue;     // 총 지원 금액
     double progress;      // 진행률
     double expectedIncome;  // 예상 수익
     double refundRate; // 반환 비율
     String inflationRate;

//     long allowance; //받기로 한 금액(임시 한도 있을시 임시한도로 설정)
//     List<Long> finalamounts;// 월별 실제 사용한 금액
     List<ExpectedIncomeEntity> incomes;

}
