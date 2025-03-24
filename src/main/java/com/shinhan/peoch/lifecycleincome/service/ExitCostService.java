package com.shinhan.peoch.lifecycleincome.service;

import com.shinhan.entity.ExpectedIncomeEntity;
import com.shinhan.entity.InvestmentEntity;
import com.shinhan.entity.PaymentEntity;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.lifecycleincome.DTO.ExitResponseDTO;
import com.shinhan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ExitCostService {
    private final double rateofreturn = 0.15;

    @Autowired
    InvestmentService investmentService;

    @Autowired
    InvestmentRepository investmentRepository;

    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    ExpectedIncomeRepository expectedIncomeRepository;

    @Autowired
    UserProfileRepository userProfileRepository;
    @Autowired
    UserRepository userRepository;


    public ExitResponseDTO exitResponseService(Integer userId){
        //optional로 예외 처리

        UserProfileEntity userProfileEntityLast =  userProfileRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("사용자 프로필 정보를 찾을 수 없습니다."));

        UserProfileEntity userProfileEntityFirst =  userProfileRepository.findFirstByUserIdOrderByUpdatedAtAsc(userId)
                .orElseThrow(() -> new RuntimeException("사용자 프로필 정보를 찾을 수 없습니다."));

        Integer userProfileIdFirst =  userProfileEntityFirst.getUserProfileId();
        Integer userProfileIdLast=  userProfileEntityLast.getUserProfileId();

        InvestmentEntity investmentEntity = investmentService.findInvestmentByUserId(userId) .orElseThrow(() -> new RuntimeException("사용자 투자 정보를 찾을 수 없습니다."));
        ExpectedIncomeEntity firstExpectedIncomeEntity = expectedIncomeRepository.findFirstByUserProfileOrderByCreatedAtDesc(userProfileEntityFirst).orElseThrow(() -> new RuntimeException("사용자 예상 소득 정보를 찾을 수 없습니다."));
        ExpectedIncomeEntity lastExpectedIncomeEntity = expectedIncomeRepository.findFirstByUserProfileOrderByCreatedAtAsc(userProfileEntityLast).orElseThrow(() -> new RuntimeException("사용자 예상 소득 정보를 찾을 수 없습니다."));

        ExitResponseDTO exitResponseDTO= ExitResponseDTO.builder()
                .firstExpectedIncome(firstExpectedIncomeEntity.getExpectedIncome())
                .lastExpectedIncome(lastExpectedIncomeEntity.getExpectedIncome())
                .discountAmount(paymentRepository.sumDiscountAmountByUserId(userId).orElse(0L))
                .investValue(paymentRepository.sumFinalAmountByUserId(userId).orElse(0L))
                .exitCost(calculateExitCost(userId))
                .StartDate(String.valueOf(investmentEntity.getStartDate()))
                .EndDate(String.valueOf(investmentEntity.getEndDate()))
                .build();

        return exitResponseDTO;
    }

    /**
     * 그냥 수익률 연산
     * 전체 쓴돈을 년을 기준으로 현재까지 기간 만큼 1.15^n년 연산
     *  사실 청구서가 있어야하는데???
     *
     *  진행률에 따라 차감인데
     *  미납 환급금을 더해서 구함
     *
     *
     *  청구서 빼면 엑시트 비용은 어떻게...?
     * @param userId
     * @return long exitCost
     */
    public long calculateExitCost(long userId){
        long exitCost = 0;
        List<PaymentEntity> paymentList = paymentRepository.findByCard_User_UserId(userId);
        for (PaymentEntity payment : paymentList) {
            int paymentyear = payment.getDate().getYear();
            int nowYear = LocalDate.now().getYear();
            exitCost += (long) (payment.getFinalAmount()*Math.pow(1+rateofreturn,nowYear-paymentyear));
        }
        //invest의 enddate(환급시작일)부터 55세 생일까지 월을 구하기
        InvestmentEntity investmentEntity = investmentRepository.findInvestmentByUserId(userId);
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        int monthsToFiftyFifth = calculateMonthsToFiftyFifth(investmentEntity.getEndDate(), userEntity.getBirthdate());
        //오늘날짜가 몇%에 해당되는지 확인해서 진행도 구하기
        // 진행도 계산 (%)
        double progressPercentage = calculateProgressPercentage(investmentEntity.getEndDate(), userEntity.getBirthdate());
        //진행도만큼 exitcost감소
        exitCost*= (long) (1-progressPercentage);
        //환급금 시스템 미완으로 인한 임시 완성처리
        //환급금 만큼 추가한 후
//        exitCost+=
        //납부 환급금 만큼 제함

        //물가 상승률 배제
        return exitCost;
    }
    public int calculateMonthsToFiftyFifth(LocalDate endDate, LocalDate birthdate) {
        // 55세 생일 계산
        LocalDate fiftyFifth= birthdate.plusYears(55);

        // 투자 종료일부터 55세 생일까지의 기간 계산
        Period period = Period.between(endDate, fiftyFifth);

        // 총 월 수 계산 (연도 * 12 + 월)
        return period.getYears() * 12 + period.getMonths();
    }

    /**
     * endDate 기준
     * 생일로부터 55세가 되는 시점까지의 진행률
     *
     * @param endDate    진행률을 계산할 기준 날짜
     * @param birthdate  생일
     * @return           55세까지의 진행률
     */
    public double calculateProgressPercentage(LocalDate endDate, LocalDate birthdate) {
        LocalDate fiftyFifthBirthday = birthdate.plusYears(55);
        int totalMonths = calculateMonthsToFiftyFifth(endDate, birthdate);

        if (totalMonths <= 0) {
            return 100.0; // 이미 55세 이후 인 경우
        }

        LocalDate currentDate = LocalDate.now();
        int elapsedMonths = (int) ChronoUnit.MONTHS.between(endDate, currentDate);

        if (elapsedMonths < 0) {
            return 0.0;
        }

        double progressPercentage = (double) elapsedMonths / totalMonths * 100;
        return Math.min(progressPercentage, 100.0);
    }
}
