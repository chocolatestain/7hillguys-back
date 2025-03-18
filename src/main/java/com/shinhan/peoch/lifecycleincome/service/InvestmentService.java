package com.shinhan.peoch.lifecycleincome.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinhan.entity.*;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.auth.service.UserService;
import com.shinhan.peoch.lifecycleincome.DTO.*;
import com.shinhan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestmentService {
    private final double rateofreturn = 0.15;
    @Autowired
    ExpectedIncomeRepository expectedIncomeRepository;
    @Autowired
    InflationRateRepository inflationRateRepository;
    @Autowired
    ExpectedValueService expectedValueService;
    @Autowired
    InvestmentRepository investmentRepository;
    @Autowired
    UserService userService;
    @Autowired
    ExpectedIncomeService expectedIncomeService;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    UserProfileRepository userProfileRepository;

    // 투자 정보 저장
    public InvestmentEntity saveInvestment(InvestmentEntity investment) {
        return investmentRepository.save(investment);
    }

    // 특정 투자 정보 조회 (ID로 조회)
    public Optional<InvestmentEntity> findInvestmentById(Integer grantId) {
        return investmentRepository.findById(grantId);
    }
    // 특정 투자 정보 조회 (userID로 조회)
    public Optional<InvestmentEntity> findInvestmentByUserId(Integer userID) {
        return Optional.ofNullable(investmentRepository.findInvestmentByUserId(userID));
    }



    // 모든 투자 정보 조회
    public List<InvestmentEntity> findAllInvestments() {
        return investmentRepository.findAll();
    }

    // 투자 정보 업데이트(사용 X)
    public InvestmentEntity updateInvestment(Integer grantId, InvestmentEntity updatedInvestment) {
        return investmentRepository.findById(grantId).map(investment -> {
            investment.setUserId(updatedInvestment.getUserId());
            investment.setExpectedIncome(updatedInvestment.getExpectedIncome());
            investment.setStartDate(updatedInvestment.getStartDate());
            investment.setEndDate(updatedInvestment.getEndDate());
            investment.setStatus(updatedInvestment.getStatus());
            investment.setOriginalInvestValue(updatedInvestment.getOriginalInvestValue());
            investment.setMonthlyAllowance(updatedInvestment.getMonthlyAllowance());
            investment.setIsActive(updatedInvestment.getIsActive());
            investment.setRefundRate(updatedInvestment.getRefundRate());
            investment.setMaxInvestment(updatedInvestment.getMaxInvestment());
            investment.setField(updatedInvestment.getField());
            investment.setInvestValue(updatedInvestment.getInvestValue());
            investment.setTempAllowance(updatedInvestment.getTempAllowance());
            return investmentRepository.save(investment);
        }).orElseThrow(() -> new IllegalArgumentException("해당 투자 정보를 찾을 수 없습니다. ID: " + grantId));
    }

    // 투자 정보 삭제? 사용 X active false로 사용할것
    public void deleteInvestment(Integer userId) {
        if (investmentRepository.existsById(userId)) {
            investmentRepository.deleteById(userId);
        } else {
            throw new IllegalArgumentException("해당 투자 정보를 찾을 수 없습니다. ID: " + userId);
        }
    }
    public InvestmentEntity createInvestment(Integer userId) {
        // 예상 소득 데이터 가져오기
        List<ExpectedIncomeEntity> incomeEntities = expectedIncomeRepository.findByUserProfileId(userId);
        if (incomeEntities.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        // 가장 최근의 예상 소득 데이터 사용
        ExpectedIncomeEntity latestIncomeEntity = incomeEntities.get(incomeEntities.size() - 1);
        Map<Integer, Double> expectedIncome = parseJsonToMap(latestIncomeEntity.getExpectedIncome());

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
        // 나이 가져와!
        UserEntity user = userService.getUserById(Long.valueOf(userId));
        // InvestmentEntity 생성 및 저장
        // 연 수익률 계산
        LocalDate endDate = calculateEndDate(user.getBirthdate());
        // 3. 연 수익률 계산
        double annualizedReturnRate = calculateAnnualizedReturnRate(
                LocalDate.now(),  // 시작일: 현재 날짜
                endDate,          // 종료일: 55세 생일
                rateofreturn
        );
        // InvestmentEntity 생성 및 저장
        InvestmentEntity investment = InvestmentEntity.builder()
                .userId(userId)
                .expectedIncome(latestIncomeEntity.getExpectedIncome())
                .status(InvestmentStatus.대기) // 승인 대기중 상태
                .originalInvestValue(0L)
                .monthlyAllowance(0)
                .isActive(false)
                .maxInvestment((int)((totalPresentValue)*(0.2)/(1+annualizedReturnRate))) // 연 수익률 적용
                .investValue(0L)
                .refundRate(0.0)
                .tempAllowance(0)
                .startDate(LocalDate.now()) // 투자 시작 날짜를 임시로 설정(투자 승인날에 업데이트 해줘야함)
                .endDate(LocalDate.now()) // 사용자가 정한 투자 기간 최대 10년
                .build();

        return investmentRepository.save(investment);
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

    public double updateRefundRate(Integer userId) {
        InvestmentEntity investment = investmentRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 투자 정보를 찾을 수 없습니다."));;

        UserEntity user = userService.getUserById(Long.valueOf(userId));
        // InvestmentEntity 생성 및 저장
        // 연 수익률 계산
        LocalDate endDate = calculateEndDate(user.getBirthdate());
        // 연 수익률 계산
        double annualizedReturnRate = calculateAnnualizedReturnRate(investment.getStartDate(), endDate, rateofreturn);

        double presentValue = expectedValueService.calculatePresentValue(userId);
        double refundRate = ((investment.getOriginalInvestValue() * (1+annualizedReturnRate)) / presentValue * 100);
        refundRate = Math.round(refundRate * 1000) / 1000.0;

        investment.setRefundRate(refundRate);
        investmentRepository.save(investment);

        return refundRate;
    }

    /**
     * 투자액과 최대 투자 금액을 주면
     * 환급 비율을 돌려줌
     * @param investAmount,maxInvestment
     * @return
     */
    public double checkRefundRate(Integer userId,Integer investAmount) {
        InvestmentEntity investment = investmentRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 투자 정보를 찾을 수 없습니다."));;

        UserEntity user = userService.getUserById(Long.valueOf(userId));
        // InvestmentEntity 생성 및 저장
        // 연 수익률 계산
        LocalDate endDate = calculateEndDate(user.getBirthdate());
        // 연 수익률 계산
        double annualizedReturnRate = calculateAnnualizedReturnRate(investment.getStartDate(), endDate, rateofreturn);

        double presentValue = expectedValueService.calculatePresentValue(userId);
        double refundRate = ((investAmount* (1+annualizedReturnRate)) / presentValue * 100);
        refundRate = Math.round(refundRate * 1000) / 1000.0;

        investment.setRefundRate(refundRate);
        investmentRepository.save(investment);

        return refundRate;
    }
    // 연 수익률 계산 메서드
    private double calculateAnnualizedReturnRate(LocalDate startDate, LocalDate endDate, double rateOfReturn) {
        long months = ChronoUnit.MONTHS.between(startDate, endDate);
        if (months == 0) throw new IllegalArgumentException("투자 기간은 1개월 이상입니다.");
        double years = (double) months / 12;
        return Math.pow(1 + rateOfReturn, 1 / years) - 1;
    }
    /**
     * 사용자 월 소득과 환급 비율을 기반으로 계산된 환급 금액 반환
     *
     * @param userId            사용자 ID
     * @param userMonthlyIncome 사용자 월 소득
     * @return 계산된 환급 금액
     */
    public int calculateRefundAmount(Integer userId, int userMonthlyIncome) {
        // 투자 정보 가져오기
        InvestmentEntity investment = investmentRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 투자 정보를 찾을 수 없습니다."));

        // 환급 비율 가져오기
        double refundRate = investment.getRefundRate();
        if (refundRate <= 0) {
            throw new IllegalArgumentException("유효하지 않은 환급 비율입니다. 사용자 ID: " + userId);
        }

        // 월 소득에 환급 비율을 곱하여 계산
        int refundAmount = (int) Math.round(userMonthlyIncome * refundRate);

        return refundAmount;
    }
    public InvestmentTempAllowanceDTO calculateInvestmentDetails(Integer userId) {
//        System.out.println("여긴 calculateInvestmentDetails");
//        System.out.println(userId);
        // 투자 데이터 조회
        InvestmentEntity investment = investmentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        LocalDate startDate = investment.getStartDate();
        LocalDate endDate = investment.getEndDate();
        long maxTotalInvestment = investment.getMaxInvestment();
        // 원래는 이게 맞는 로직
//        long investValue = investment.getInvestValue();

        //투자한 돈의 총합 없을경우 0으로 리턴
        long investValue = paymentRepository.sumFinalAmountByUserId(userId).orElse(0L);
        // 오늘 날짜 기준 진행률 계산
        double progress = calculateInvestmentProgress(startDate, endDate);

        // 현재 지원 가능 금액 계산
        long availableAmount = (long) (maxTotalInvestment * progress);

        // 예상 생애 총소득 총액
        double expectedIncome = expectedValueService.calculatePresentValue(userId);

        // 인플레이션
        InflationRateEntity inflationRateEntity = inflationRateRepository.findByYear(LocalDate.now().getYear());
        String inflationRate = inflationRateEntity.getInflationRate();
        double refundRate = updateRefundRate(userId);
        //그래프용 데이터
        UserProfileEntity userProfileEntity = userProfileRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
        List<ExpectedIncomeEntity> incomes = expectedIncomeService.getExpectedIncomesByUserProfileId(userProfileEntity.getUserProfileId());
        // 결과 반환
        return new InvestmentTempAllowanceDTO(availableAmount, investValue, progress, expectedIncome, refundRate, inflationRate,incomes);
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

    /**
     * investment 세팅
     *
     * @param userId
     * @param setAmountRequestDTO
     * @return 투자 설정 저장 결과를 담은 ApiResponseDTO 객체 (성공 시 성공 메시지, 실패 시 오류 메시지와 코드 포함)
     */

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

            return ApiResponseDTO.success("투자 설정이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ApiResponseDTO.error("투자 설정 중 오류가 발생했습니다: " + e.getMessage(), "INVESTMENT_SETTING_ERROR");
        }

    }


    public ApiResponseDTO<String> setTempAllowance(Integer amount, Long userId) {
        try {
            InvestmentEntity investment = investmentRepository.findInvestmentByUserId(userId);

            if (investment == null) {
                return ApiResponseDTO.error("사용자의 투자 정보를 찾을 수 없습니다", "USER_NOT_FOUND");
            }

            if (investment.getMaxInvestment() < amount) {
                return ApiResponseDTO.error("요청한 임시 한도가 최대 투자 한도를 초과합니다", "EXCEED_MAX_INVESTMENT");
            }

            investment.setTempAllowance(amount);
            investmentRepository.save(investment);

            return ApiResponseDTO.success("임시 한도 설정 완료");
        } catch (Exception e) {
            return ApiResponseDTO.error("임시 한도 설정 중 오류가 발생했습니다: " + e.getMessage(), "INTERNAL_ERROR");
        }
    }



}
