package com.shinhan.peoch.lifecycleincome.service;

import com.shinhan.entity.ExpectedIncomeEntity;
import com.shinhan.entity.InflationRateEntity;
import com.shinhan.entity.InvestmentEntity;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.peoch.lifecycleincome.DTO.ApiResponseDTO;
import com.shinhan.peoch.lifecycleincome.DTO.SetAmountRequestDTO;
import com.shinhan.peoch.lifecycleincome.DTO.SetInvestAmountDTO;
import com.shinhan.repository.ExpectedIncomeRepository;
import com.shinhan.repository.InflationRateRepository;
import com.shinhan.repository.InvestmentRepository;
import com.shinhan.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SetInvestAmountService {
    @Autowired
    InvestmentRepository investmentRepository;
    @Autowired
    ExpectedIncomeRepository expectedIncomeRepository;
    @Autowired
    InflationRateRepository inflationRateRepository;
    @Autowired
    ExitCostService exitCostService;

    @Autowired
    UserProfileRepository userProfileRepository;

    public SetInvestAmountDTO getInvestmentData(Integer userProfileId) {
        UserProfileEntity userProfileEntity = userProfileRepository.findByUserProfileId(userProfileId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저 프로필 정보를 찾을 수 없습니다."));
        // 1. 투자 정보 조회
        InvestmentEntity investment = investmentRepository.findFirstByUserIdOrderByUpdatedAtDesc(userProfileEntity.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("해당 투자 정보를 찾을 수 없습니다."));
        System.out.println(investment);
        // 2. 예상 소득 데이터 조회
        ExpectedIncomeEntity incomes = expectedIncomeRepository.findFirstByUserProfileIdOrderByCreatedAtDesc(userProfileId).
        orElseThrow(() -> new RuntimeException("사용자 예상 소득 정보를 찾을 수 없습니다."));


        // 3. 물가상승률 데이터 조회 (2025년 고정)
        InflationRateEntity inflationRateEntity = inflationRateRepository.findByYear(LocalDate.now().getYear());


        return SetInvestAmountDTO.builder()
                .maxInvestment(Long.valueOf(investment.getMaxInvestment()))
                .expectedIncomes(incomes.getExpectedIncome())
                .inflationRate(inflationRateEntity.getInflationRate())
                .build();
    }
    public ApiResponseDTO<String> stopInvestment(Long userId) {
        /**
         *   기존 엑시트 비용함수랑 누적 환급 금액이 일치하거나 더 많으면 엑시트 비용은 그 시점에서
         */

        InvestmentEntity investment = investmentRepository.findInvestmentByUserId(userId);
        long exitCost = exitCostService.calculateExitCost(userId);
        if (exitCost>0){
            return ApiResponseDTO.error("아직 남은 환급금이 존재합니다. ","exitcost가 0보다 큼");
        }
        else {
            //isActive false
            investment.setIsActive(false);
            investmentRepository.save(investment);
            return ApiResponseDTO.success("투자 계약 조기 해지 완료");
        }


    }
    public ApiResponseDTO<String> setInvestment(Long userId, SetAmountRequestDTO setAmountRequestDTO) {
        try {
            InvestmentEntity investment = investmentRepository.findInvestmentByUserId(userId);
            investment.setRefundRate(0D);
            investment.setMonthlyAllowance(setAmountRequestDTO.getMonthlyAmount());

            // 시작일은 오늘
            LocalDate startDate = LocalDate.now();
            investment.setStartDate(startDate);

            // 종료일은 오늘 + period년
            LocalDate endDate = startDate.plusYears(setAmountRequestDTO.getPeriod());
            investment.setEndDate(endDate);

            investmentRepository.save(investment);

            return new ApiResponseDTO<>(true, "투자 설정이 성공적으로 저장되었습니다.", null);
        } catch (Exception e) {
            return new ApiResponseDTO<>(false, "투자 설정 중 오류가 발생했습니다: " + e.getMessage(), null);
        }
    }

}
