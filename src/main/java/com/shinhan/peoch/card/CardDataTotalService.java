package com.shinhan.peoch.card;

import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.repository.PaymentRepository;
import com.shinhan.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

@Service
public class CardDataTotalService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public CardDataTotalService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    // 카드 데이터(Total)
    public CardDataTotalResponseDTO getCardDataTotal(Long userId, String requestDate) {
        LocalDate date = LocalDate.parse(requestDate);

        // ===== 주 데이터 계산 =====
        // 현재 주: 요청일 기준 해당 주의 월요일부터 요청일(오늘)까지
        LocalDate currentWeekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int daysCount = date.getDayOfWeek().getValue(); // 월요일=1, ... 일요일=7

        // 저번주: 현재 주 시작일에서 1주 전의 월요일부터 (동일한 일수만큼)
        LocalDate previousWeekStart = currentWeekStart.minusWeeks(1);
        LocalDate previousWeekEnd = previousWeekStart.plusDays(daysCount - 1);

        // ===== 월 데이터 계산 =====
        // 현재 달: 요청일의 1일부터 요청일까지
        LocalDate currentMonthStart = date.withDayOfMonth(1);
        int currentDayOfMonth = date.getDayOfMonth();

        // 전 달: 요청일의 일자가 전 달에 존재하면 해당 일자, 아니면 전 달의 마지막 날
        LocalDate previousMonthDate = date.minusMonths(1);
        LocalDate previousMonthStart = previousMonthDate.withDayOfMonth(1);
        int previousMonthLength = previousMonthDate.lengthOfMonth();
        LocalDate previousMonthEnd = (currentDayOfMonth <= previousMonthLength)
                ? previousMonthDate.withDayOfMonth(currentDayOfMonth)
                : previousMonthDate.withDayOfMonth(previousMonthLength);

        // ===== 연 데이터 계산 =====
        // 현재 연: 해당 연도의 1월 1일부터 요청일까지
        LocalDate currentYearStart = LocalDate.of(date.getYear(), 1, 1);
        // 작년: 이전 연도의 1월 1일부터 요청일과 동일한 월/일 (단, 윤년 등은 별도 고려 필요)
        LocalDate previousYearDate = date.minusYears(1);
        LocalDate previousYearStart = LocalDate.of(previousYearDate.getYear(), 1, 1);
        LocalDate previousYearEnd = LocalDate.of(previousYearDate.getYear(), date.getMonthValue(), date.getDayOfMonth());

        // ===== 내 총 사용 금액 계산 =====
        int weekCurrentTotal = getTotalFinalAmount(userId, currentWeekStart, date);
        int weekPreviousTotal = getTotalFinalAmount(userId, previousWeekStart, previousWeekEnd);
        int monthCurrentTotal = getTotalFinalAmount(userId, currentMonthStart, date);
        int monthPreviousTotal = getTotalFinalAmount(userId, previousMonthStart, previousMonthEnd);
        int yearCurrentTotal = getTotalFinalAmount(userId, currentYearStart, date);
        int yearPreviousTotal = getTotalFinalAmount(userId, previousYearStart, previousYearEnd);

        // ===== 동갑 평균 사용 금액 계산 =====
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        int age = Period.between(user.getBirthdate(), LocalDate.now()).getYears();

        int avgWeekCurrentTotal = getAverageTotalFinalAmount(age, currentWeekStart, date);
        int avgWeekPreviousTotal = getAverageTotalFinalAmount(age, previousWeekStart, previousWeekEnd);
        int avgMonthCurrentTotal = getAverageTotalFinalAmount(age, currentMonthStart, date);
        int avgMonthPreviousTotal = getAverageTotalFinalAmount(age, previousMonthStart, previousMonthEnd);
        int avgYearCurrentTotal = getAverageTotalFinalAmount(age, currentYearStart, date);
        int avgYearPreviousTotal = getAverageTotalFinalAmount(age, previousYearStart, previousYearEnd);

        // ===== DTO 반환 =====
        return new CardDataTotalResponseDTO(
                weekCurrentTotal,
                weekPreviousTotal,
                monthCurrentTotal,
                monthPreviousTotal,
                yearCurrentTotal,
                yearPreviousTotal,
                avgWeekCurrentTotal,
                avgWeekPreviousTotal,
                avgMonthCurrentTotal,
                avgMonthPreviousTotal,
                avgYearCurrentTotal,
                avgYearPreviousTotal
        );
    }

    // 해당 기간 총 사용 금액 조회
    private int getTotalFinalAmount(Long useId, LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        return paymentRepository.findTotalFinalAmountByUserAndDateBetween(useId, startDateTime, endDateTime);
    }

    // 동일한 나이의 평균 사용자가 사용한 총 금액 반환
    private int getAverageTotalFinalAmount(int age, LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        LocalDate today = LocalDate.now();

        LocalDate birthDateEnd = today.minusYears(age);
        LocalDate birthDateStart = today.minusYears(age + 1).plusDays(1);

        Double avg = paymentRepository.findAverageTotalFinalAmountByBirthdateBetween(birthDateStart, birthDateEnd, startDateTime, endDateTime);
        return (avg != null) ? avg.intValue() : 0;
    }

}

