package com.shinhan.peoch.card;

import com.shinhan.entity.*;
import com.shinhan.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final PaymentRepository paymentRepository;
    private final MyBenefitRepository myBenefitRepository;
    private final BenefitRepository benefitRepository;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, PaymentRepository paymentRepository,
                       BenefitRepository benefitRepository, MyBenefitRepository myBenefitRepository,
                       UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.paymentRepository = paymentRepository;
        this.benefitRepository = benefitRepository;
        this.myBenefitRepository = myBenefitRepository;
        this.userRepository = userRepository;
    }

    // 카드 명세서
    public CardStatementResponseDTO getCardStatement(Long userId, String yearMonth) {
        // yearMonth = String("yyyy-MM"), yearMonth가 없으면 현재 연도/월을 사용
        LocalDateTime[] dateRange = getDate(yearMonth);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        List<PaymentEntity> payments = paymentRepository.findByCard_User_UserIdAndDateBetween(userId, startDate, endDate);
        List<CardStatementDTO> statementList = new ArrayList<>();
        int monthlySpent = 0;

        for (PaymentEntity payment : payments) {
            if (payment.getStatus() == PaymentStatus.PAID) {
                monthlySpent += payment.getFinalAmount();
            }

            CardStatementDTO dto = convertToCardStatementDTO(payment);
            statementList.add(dto);
        }

        // 결제일 내림차순(가장 최신 거래가 맨 앞)
        statementList.sort(Comparator.comparing(CardStatementDTO::getPaymentDate).reversed());

        CardEntity card = cardRepository.findByUser_UserId(userId);
        Integer monthlyAllowance = card.getMonthlyAllowance();

        CardStatementResponseDTO response = new CardStatementResponseDTO();
        response.setStatementList(statementList);
        response.setMonthlyAllowance(monthlyAllowance);
        response.setMonthlySpent(monthlySpent);
        return response;
    }

    // 결제완료, 할부 내역 CardStatementDTO로 변환
    private CardStatementDTO convertToCardStatementDTO(PaymentEntity payment) {
        CardStatementDTO dto = new CardStatementDTO();
        dto.setAmount(payment.getFinalAmount());
        dto.setPaymentDate(payment.getDate());
        dto.setStoreName(payment.getStore().getName());

        // status에 따라 분기 처리
        switch (payment.getStatus()) {
            case PENDING:
                dto.setPaymentStatus(PaymentStatus.PENDING);
                dto.setInstallmentMonth(payment.getInstallmentMonth() != null ? payment.getInstallmentMonth() : 0);
                dto.setInstallmentRound(payment.getInstallmentRound() != null ? payment.getInstallmentRound() : 0);
                dto.setBenefitDiscountAmount(payment.getDiscountAmount());
                break;
            case PAID:
                dto.setPaymentStatus(PaymentStatus.PAID);
                dto.setInstallmentMonth(0);
                dto.setInstallmentRound(0);
                dto.setBenefitDiscountAmount(payment.getDiscountAmount());
                break;
            case REFUNDED:
                dto.setPaymentStatus(PaymentStatus.REFUNDED);
                dto.setInstallmentMonth(0);
                dto.setInstallmentRound(0);
                dto.setBenefitDiscountAmount(0);
                break;
        }
        return dto;
    }

    // 환불 내역 CardStatementDTO로 변환
    private CardStatementDTO convertRefundToDTO(RefundEntity refund) {
        CardStatementDTO dto = new CardStatementDTO();
        dto.setAmount(refund.getAmount());
        dto.setPaymentDate(refund.getDate());
        dto.setPaymentStatus(PaymentStatus.REFUNDED);
        dto.setStoreName(refund.getPayment().getStore().getName());
        dto.setInstallmentMonth(0);
        dto.setInstallmentRound(0);
        dto.setBenefitDiscountAmount(0);
        return dto;
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    // 혜택 명세서
    public BenefitStatementResponseDTO getCardPerformance(Long userId, String yearMonth) {
        // yearMonth = String("yyyy-MM"), yearMonth가 없으면 현재 연도/월을 사용
        LocalDateTime[] dateRange = getDate(yearMonth);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        List<PaymentEntity> payments = paymentRepository.findByCard_User_UserIdAndDateBetween(userId, startDate, endDate);
        List<BenefitStatementDTO> statementList = new ArrayList<>();
        int totalBenefitDiscount = 0;
        String userName = userRepository.findNameById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다."));

        for (PaymentEntity p : payments) {
            if (p.getDiscountAmount() != 0) {
                totalBenefitDiscount += p.getDiscountAmount();
                BenefitStatementDTO dto = convertToBenefitStatementDTO(p);
                statementList.add(dto);
            }
        }

        // 결제일 내림차순(가장 최신 거래가 맨 앞)
        statementList.sort(Comparator.comparing(BenefitStatementDTO::getPaymentDate).reversed());

        BenefitStatementResponseDTO response = new BenefitStatementResponseDTO();
        response.setStatementList(statementList);
        response.setTotalBenefitDiscount(totalBenefitDiscount);
        response.setUserName(userName);
        return response;
    }

    // BenefitStatementDTO로 변환
    private BenefitStatementDTO convertToBenefitStatementDTO(PaymentEntity payment) {
        BenefitStatementDTO dto = new BenefitStatementDTO();
        dto.setStoreName(payment.getStore().getName());
        dto.setPaymentDate(payment.getDate());
        dto.setCardNumber(payment.getCard().getCardNumber());
        dto.setOriginalAmount(payment.getOriginalAmount());
        dto.setFinalAmount(payment.getFinalAmount());
        dto.setBenefitDiscountAmount(payment.getDiscountAmount());
        return dto;
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    // 전체 혜택 조회
    public List<AllBenefitDTO> getAllBenefit(Long userId) {
        // yearMonth == null이면 현재 연도/월을 사용
        LocalDateTime[] dateRange = getDate(null);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        // 1. 사용자가 현재 사용 중인 혜택 ID 목록 조회
        List<Long> myBenefitIds = myBenefitRepository.findBenefitIdsByUserId(userId);

        // 2. 전체 혜택 목록 조회
        List<BenefitEntity> allBenefits = benefitRepository.findAll();

        List<AllBenefitDTO> AllBenefitList = new ArrayList<>();

        // 3. 각 혜택별로 DTO 변환 처리
        for (BenefitEntity benefit : allBenefits) {
            AllBenefitDTO dto = convertToAllBenefitDTO(userId, benefit, myBenefitIds, startDate, endDate);
            AllBenefitList.add(dto);
        }

        return AllBenefitList;
    }

    // AllBenefitDTO로 변환
    private AllBenefitDTO convertToAllBenefitDTO(Long userId, BenefitEntity benefit, List<Long> myBenefitIds,
                                                 LocalDateTime startDate, LocalDateTime endDate) {
        AllBenefitDTO dto = new AllBenefitDTO();
        dto.setBenefit(benefit);
        dto.setStoreName(benefit.getStore().getName());

        // 사용 중인 혜택이면 inUse=true, 놓친 혜택 금액은 0
        if (myBenefitIds.contains(benefit.getBenefitId())) {
            dto.setInUse(true);
            dto.setMissedBenefitAmount(0L);
        } else {
            dto.setInUse(false);
            dto.setMissedBenefitAmount(calculateMissedBenefit(userId, benefit, startDate, endDate));
        }

        return dto;
    }

    // 놓친 혜택 계산
    private long calculateMissedBenefit(Long userId, BenefitEntity benefit, LocalDateTime startDate, LocalDateTime endDate) {
        Long storeId = benefit.getStore().getStoreId();
        List<PaymentEntity> payments = paymentRepository.findByCard_User_UserIdAndStore_StoreIdAndDateBetween(
                userId, storeId, startDate, endDate);

        long missedAmount = 0;
        for (PaymentEntity payment : payments) {
            // 결제 원금이 혜택 적용 최소 결제액 이상일 때 혜택 적용
            if (payment.getOriginalAmount() >= benefit.getMinPayment()) {
                // 할인 금액 계산
                long discount = Math.round(payment.getOriginalAmount() * (benefit.getDiscountRate() / 100.0));
                // 최대 할인 금액 초과 시 최대값 적용
                missedAmount += Math.min(discount, benefit.getMaxDiscount());
            }
        }
        return missedAmount;
    }


    // 날짜 반환
    private LocalDateTime[] getDate(String yearMonth) {
        YearMonth ym = (yearMonth == null || yearMonth.isEmpty())
                ? YearMonth.now()
                : YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"));

        LocalDateTime startDate = ym.atDay(1).atStartOfDay();
        LocalDateTime endDate = ym.atEndOfMonth().atTime(23, 59, 59, 999999999);

        return new LocalDateTime[]{startDate, endDate};
    }

}
