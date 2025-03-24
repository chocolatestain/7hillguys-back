package com.shinhan.peoch.lifecycleincome.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinhan.entity.ExpectedIncomeEntity;
import com.shinhan.entity.InflationRateEntity;
import com.shinhan.entity.InvestmentEntity;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.auth.service.UserService;
import com.shinhan.peoch.lifecycleincome.DTO.*;
import com.shinhan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SetInvestAmountService {
    private final double rateofreturn = 0.15;
    @Autowired
    InvestmentRepository investmentRepository;
    @Autowired
    ExpectedIncomeRepository expectedIncomeRepository;
    @Autowired
    InflationRateRepository inflationRateRepository;
    @Autowired
    ExitCostService exitCostService;
    @Autowired
    InvestmentService investmentService;
    @Autowired
    UserProfileRepository userProfileRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    UserService userService;

    public SetInvestAmountDTO getInvestmentData(Integer userProfileId) {

        UserProfileEntity userProfileEntity = userProfileRepository.getReferenceById(userProfileId);

        // 1. 투자 정보 조회
        InvestmentEntity investment = investmentRepository.findFirstByUserIdOrderByUpdatedAtDesc(userProfileEntity.getUserId())
                .orElseThrow(() -> new RuntimeException("해당 투자 정보를 찾을 수 없습니다."));
        System.out.println(investment);

        // 2. 예상 소득 데이터 조회
        ExpectedIncomeEntity incomes = expectedIncomeRepository.findFirstByUserProfileOrderByCreatedAtDesc(userProfileEntity).
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
         *   남은 기간 %로만 적용
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
            // 사용자의 투자 정보를 조회
            InvestmentEntity investment = investmentRepository.findInvestmentByUserId(userId);
            if (investment == null) {
                return ApiResponseDTO.error("투자 정보를 찾을 수 없습니다.", "INVESTMENT_NOT_FOUND");
            }

            // 투자 정보 설정
            investment.setRefundRate(0D);
            investment.setMonthlyAllowance(setAmountRequestDTO.getMonthlyAmount());

            // 오늘 기준 설정
            LocalDate startDate = LocalDate.now();
            if (investment.getStartDate() == null) {
                investment.setStartDate(startDate);
                // 종료일은 오늘부터 입력한 년수 만큼
                LocalDate endDate = startDate.plusYears(setAmountRequestDTO.getPeriod());
                investment.setEndDate(endDate);
            }
            LocalDate endDate = LocalDate.now().plusYears(setAmountRequestDTO.getPeriod());
            investment.setEndDate(endDate);

            // 예상 소득 데이터 가져오기
            Map<Integer, Double> expectedIncome = parseJsonToMap(investment.getExpectedIncome());

            // 국채 수익률 데이터 가져오기
            InflationRateEntity inflationRateEntity = inflationRateRepository.findByYear(2025);
            Map<Integer, Double> inflationRates = parseJsonToMap(inflationRateEntity.getInflationRate());

            // 현재 가치 계산
            double totalPresentValue = 0.0;
            for (Map.Entry<Integer, Double> entry : expectedIncome.entrySet()) {
                int year = entry.getKey();
                double income = entry.getValue();
                double discountRate = getDiscountRate(year, inflationRates);
                totalPresentValue += calculatePresentValueForIncome(income, year, discountRate);
            }

            // 나이 및 연 수익률 계산
            UserEntity user = userService.getUserById(userId);
            LocalDate calculatedEndDate = calculateEndDate(user.getBirthdate());
            double annualizedReturnRate = calculateAnnualizedReturnRate(
                    LocalDate.now(),  // 시작일: 현재 날짜
                    calculatedEndDate, // 종료일: 사용자 나이 기반
                    rateofreturn
            );

            // maxInvestment 계산 및 설정
            investment.setMaxInvestment((int)((totalPresentValue) * 0.2 / (1 + annualizedReturnRate)));
            //반환 비율 설정
            //로직 꼬여버림 update할때 반환율이랑 set할때 반환율이 아구가 안맞음
            investment.setRefundRate(setAmountRequestDTO.getRefundRate());
            investment.setOriginalInvestValue(setAmountRequestDTO.getTotalAmount());
            // DB에 저장

            investmentRepository.save(investment);
//            investmentService.updateRefundRate(Math.toIntExact(userId));

            return ApiResponseDTO.success("투자 설정이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            // 예외 처리 및 상세 메시지 제공
            String errorMessage = "투자 설정 중 오류가 발생했습니다: " + e.getMessage();
            return ApiResponseDTO.error(errorMessage, "INTERNAL_SERVER_ERROR");
        }
    }
    private double calculatePresentValueForIncome(double income, int year, double discountRate) {
        return income / Math.pow(1 + discountRate / 100, year);
    }

    private double getDiscountRate(int year, Map<Integer, Double> inflationRates) {
        if (year <= 5) return inflationRates.getOrDefault(year, inflationRates.get(5));
        if (year <= 9) return inflationRates.get(5);
        if (year <= 19) return inflationRates.get(10);
        if (year <= 29) return inflationRates.get(20);
        if (year <= 49) return inflationRates.get(30);
        return inflationRates.get(50);
    }

    private Map<Integer, Double> parseJsonToMap(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonString, new TypeReference<Map<Integer, Double>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    private double calculateInvestmentProgress(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) return 0.0; // 시작 전
        if (today.isAfter(endDate)) return 1.0; //종료 이후

        // 총 기간 (개월 단위)
        long totalMonths = ChronoUnit.MONTHS.between(startDate, endDate) + 1; // 포함 관계를 위해 +1
        // 경과 기간 (개월 단위)
        long elapsedMonths = ChronoUnit.MONTHS.between(startDate, today) + 1;

        return (double) elapsedMonths / totalMonths; // 진행률 (0~1)
    }
    public LocalDate calculateEndDate(LocalDate birthDate) {
        LocalDate FiftyFifty  = birthDate.plusYears(55);
        return FiftyFifty;
    }
    public ReallyExitResponseDTO getInvestmentExitInfo(Integer userId) {

        InvestmentEntity investmentEntity = investmentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("투자 정보를 찾을 수 없습니다."));
        System.out.println(investmentEntity.toString());
        System.out.println(userId);
        LocalDate startDate = investmentEntity.getStartDate();
        LocalDate endDate = investmentEntity.getEndDate();
        LocalDate currentMonth = LocalDate.now();
        LocalDate lastDayOfCurrentMonth = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());

        if (endDate.isAfter(lastDayOfCurrentMonth)) {
            endDate = lastDayOfCurrentMonth;
        }

        // 기간 계산 (연도 차이)
        int startYear = startDate.getYear();
        int endYear = endDate.getYear();
        int yearDifference = endYear - startYear;

        // DB에서 물가상승률 가져오기
        InflationRateEntity inflationRateEntity = inflationRateRepository.findByYear(LocalDate.now().getYear());
        Map<Integer, Double> inflationRates = parseJsonToMap(inflationRateEntity.getInflationRate());

        // 연도별 적용할 물가상승률 구하기
        double discountRate = getDiscountRate(yearDifference, inflationRates);

        // 복리 계산용으로 연도별로 N승 해두기
        double compoundInflationRate = Math.pow(1 + (discountRate / 100), yearDifference) - 1;

        List<Object[]> monthlyPayments = paymentRepository.findMonthlyPaymentsByUserIdAndDateBetweenAndStatus(
                Long.valueOf(userId),
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay().minusSeconds(1));


        List<MonthlyPaymentDTO> monthlyPaymentDTOS = monthlyPayments.stream()
                .map(obj -> new MonthlyPaymentDTO((String)obj[0], (Long)obj[1]))
                .collect(Collectors.toList());

        long totalAmount = monthlyPaymentDTOS.stream()
                .mapToLong(MonthlyPaymentDTO::getTotalAmount).sum();

        // 계산된 인플레이션과 기간이 적용된 물가상승률 적용
        long adjustedAmount = Math.round(totalAmount * (1 + compoundInflationRate));

        return ReallyExitResponseDTO.builder()
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .monthlyPayments(monthlyPaymentDTOS)
                .totalAmount(totalAmount)
                .adjustedAmount(adjustedAmount)
                .build();
    }
    // 연 수익률 계산 메서드
    private double calculateAnnualizedReturnRate(LocalDate startDate, LocalDate endDate, double rateOfReturn) {
        long months = ChronoUnit.MONTHS.between(startDate, endDate);
        if (months == 0) throw new IllegalArgumentException("투자 기간은 1개월 이상입니다.");
        double years = (double) months / 12;
        return Math.pow(1 + rateOfReturn, 1 / years) - 1;
    }


}
