package com.shinhan.peoch.card;

import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.repository.PaymentRepository;
import com.shinhan.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CardDataMapService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public CardDataMapService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    // 카드 데이터 조회(Map)
    public CardDataMapResponseDTO getCardDataMap(Long userId, String requestDate) {
        LocalDate requestLocalDate = LocalDate.parse(requestDate);

        // 사용자 정보 및 나이 계산
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        int age = Period.between(user.getBirthdate(), LocalDate.now()).getYears();

        // ===== 주 데이터 =====
        // [이번 주] : 이번 주 월요일부터 요청일(오늘)까지
        Map<String, Integer> weeklyCurrentMap = new LinkedHashMap<>();
        Map<String, Integer> weeklyCurrentAverageMap = new LinkedHashMap<>();
        LocalDate currentMonday = requestLocalDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (LocalDate day = currentMonday; !day.isAfter(requestLocalDate); day = day.plusDays(1)) {
            int myAmount = getTotalFinalAmount(userId, day, day);
            int avgAmount = getAverageTotalFinalAmount(age, day, day);
            String koreanDay = getKoreanDayName(day.getDayOfWeek());
            weeklyCurrentMap.put(koreanDay, myAmount);
            weeklyCurrentAverageMap.put(koreanDay, avgAmount);
        }

        // [저번 주] : 저번 주 월요일부터 일요일까지
        Map<String, Integer> weeklyPreviousMap = new LinkedHashMap<>();
        Map<String, Integer> weeklyPreviousAverageMap = new LinkedHashMap<>();
        LocalDate previousMonday = currentMonday.minusWeeks(1);
        LocalDate previousSunday = previousMonday.plusDays(6);
        for (LocalDate day = previousMonday; !day.isAfter(previousSunday); day = day.plusDays(1)) {
            int myAmount = getTotalFinalAmount(userId, day, day);
            int avgAmount = getAverageTotalFinalAmount(age, day, day);
            String koreanDay = getKoreanDayName(day.getDayOfWeek());
            weeklyPreviousMap.put(koreanDay, myAmount);
            weeklyPreviousAverageMap.put(koreanDay, avgAmount);
        }

        // ===== 월 데이터 =====
        // [이번 달] : 1일부터 요청일까지 7일 단위 그룹 집계 (예: 1~7, 8~14, …)
        Map<String, Integer> monthlyCurrentMap = new LinkedHashMap<>();
        Map<String, Integer> monthlyCurrentAverageMap = new LinkedHashMap<>();
        LocalDate firstDayCurrentMonth = requestLocalDate.withDayOfMonth(1);
        LocalDate lastDayCurrentMonth = requestLocalDate.withDayOfMonth(requestLocalDate.lengthOfMonth());
        int groupIndex = 1;
        LocalDate groupStart = firstDayCurrentMonth;
        while (!groupStart.isAfter(lastDayCurrentMonth)) {
            LocalDate groupEnd = groupStart.plusDays(6);
            if (groupEnd.isAfter(lastDayCurrentMonth)) {
                groupEnd = lastDayCurrentMonth;
            }
            int myAmount = getTotalFinalAmount(userId, groupStart, groupEnd);
            int avgAmount = getAverageTotalFinalAmount(age, groupStart, groupEnd);
            monthlyCurrentMap.put(String.valueOf(groupIndex), myAmount);
            monthlyCurrentAverageMap.put(String.valueOf(groupIndex), avgAmount);
            groupIndex++;
            groupStart = groupEnd.plusDays(1);
        }

        // [전 달] : 전 달 전체를 7일 단위 그룹 집계
        Map<String, Integer> monthlyPreviousMap = new LinkedHashMap<>();
        Map<String, Integer> monthlyPreviousAverageMap = new LinkedHashMap<>();
        LocalDate previousMonthDate = requestLocalDate.minusMonths(1);
        LocalDate firstDayPreviousMonth = previousMonthDate.withDayOfMonth(1);
        LocalDate lastDayPreviousMonth = previousMonthDate.withDayOfMonth(previousMonthDate.lengthOfMonth());
        groupIndex = 1;
        groupStart = firstDayPreviousMonth;
        while (!groupStart.isAfter(lastDayPreviousMonth)) {
            LocalDate groupEnd = groupStart.plusDays(6);
            if (groupEnd.isAfter(lastDayPreviousMonth)) {
                groupEnd = lastDayPreviousMonth;
            }
            int myAmount = getTotalFinalAmount(userId, groupStart, groupEnd);
            int avgAmount = getAverageTotalFinalAmount(age, groupStart, groupEnd);
            monthlyPreviousMap.put(String.valueOf(groupIndex), myAmount);
            monthlyPreviousAverageMap.put(String.valueOf(groupIndex), avgAmount);
            groupIndex++;
            groupStart = groupEnd.plusDays(1);
        }

        // ===== 연 데이터 =====
        // [올해] : 올해 1월부터 12월까지 월별 집계
        Map<String, Integer> yearlyCurrentMap = new LinkedHashMap<>();
        Map<String, Integer> yearlyCurrentAverageMap = new LinkedHashMap<>();
        int currentYear = requestLocalDate.getYear();
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(currentYear, month, 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            int myAmount = getTotalFinalAmount(userId, monthStart, monthEnd);
            int avgAmount = getAverageTotalFinalAmount(age, monthStart, monthEnd);
            yearlyCurrentMap.put(String.valueOf(month), myAmount);
            yearlyCurrentAverageMap.put(String.valueOf(month), avgAmount);
        }

        // [작년] : 작년 1월부터 12월까지 월별 집계
        Map<String, Integer> yearlyPreviousMap = new LinkedHashMap<>();
        Map<String, Integer> yearlyPreviousAverageMap = new LinkedHashMap<>();
        int previousYear = currentYear - 1;
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(previousYear, month, 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            int myAmount = getTotalFinalAmount(userId, monthStart, monthEnd);
            int avgAmount = getAverageTotalFinalAmount(age, monthStart, monthEnd);
            yearlyPreviousMap.put(String.valueOf(month), myAmount);
            yearlyPreviousAverageMap.put(String.valueOf(month), avgAmount);
        }

        return new CardDataMapResponseDTO(
                weeklyCurrentMap, weeklyPreviousMap,
                monthlyCurrentMap, monthlyPreviousMap,
                yearlyCurrentMap, yearlyPreviousMap,
                weeklyCurrentAverageMap, weeklyPreviousAverageMap,
                monthlyCurrentAverageMap, monthlyPreviousAverageMap,
                yearlyCurrentAverageMap, yearlyPreviousAverageMap
        );
    }

    // 특정 사용기간동안 사용한 총 금액 반환
    private int getTotalFinalAmount(Long userId, LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        return paymentRepository.findTotalFinalAmountByUserAndDateBetween(userId, startDateTime, endDateTime);
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

    // DayOfWeek를 한글 요일(월, 화, 수, 목, 금, 토, 일)로 변환
    private String getKoreanDayName(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "월";
            case TUESDAY:
                return "화";
            case WEDNESDAY:
                return "수";
            case THURSDAY:
                return "목";
            case FRIDAY:
                return "금";
            case SATURDAY:
                return "토";
            case SUNDAY:
                return "일";
            default:
                return "";
        }
    }
}
