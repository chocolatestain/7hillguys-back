package com.shinhan.peoch.payment;

import com.shinhan.entity.*;
import com.shinhan.peoch.card.BenefitStatementResponseDTO;
import com.shinhan.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {
    private final CardRepository cardRepository;
    private final RefundRepository refundRepository;
    private final BenefitRepository benefitRepository;
    private final MyBenefitRepository myBenefitRepository;
    private final PaymentRepository paymentRepository;
    private final StoreRepository storeRepository;

    public PaymentService(CardRepository cardRepository, RefundRepository refundRepository,
                          BenefitRepository benefitRepository, MyBenefitRepository myBenefitRepository,
                          PaymentRepository paymentRepository, StoreRepository storeRepository) {
        this.cardRepository = cardRepository;
        this.refundRepository = refundRepository;
        this.benefitRepository = benefitRepository;
        this.myBenefitRepository = myBenefitRepository;
        this.paymentRepository = paymentRepository;
        this.storeRepository = storeRepository;
    }

    // 카드 결제 로직
    public PosResponse processPayment(PosPaymentRequest request) {
        // 요청한 결제 카드 유효성 검사
        boolean valid = validateCard(request.getCardNumber());
        if (!valid) {
            return new PosResponse(false, "유효하지 않은 카드입니다.", "NOT_VALID_CARD");
        }

        // 카드 정보 조회
        Optional<CardEntity> optionalCard = cardRepository.findByCardNumber(request.getCardNumber());
        CardEntity card = optionalCard.orElseThrow(); // 카드엔티티
        String cardNumber = card.getCardNumber(); // 카드번호

        // 한도 검사
        if (isLimitExceeded(card, request.getAmount())) {
            return new PosResponse(false, "월 한도를 초과했습니다.", "LIMIT_EXCEEDED");
        }

        // 상점 엔티티 조회
        Optional<StoreEntity> storeEntityOpt = storeRepository.findById(request.getStoreId());
        if (storeEntityOpt.isEmpty()) { // 상점이 존재하지 않으면 오류 응답 반환
            return new PosResponse(false, "유효하지 않은 상점입니다.", "NOT_VALID_STORE");
        }
        StoreEntity store = storeEntityOpt.get();

        // 혜택 적용 검사
        List<BenefitEntity> myBenfitList = myBenefitRepository.findBenefitsByCardNumber(cardNumber);
        Map<String, Integer> map = applyBenefit(myBenfitList, request, cardNumber);
        int discountAmount = map.get("discountAmount"); // 할인 금액
        int finalAmount = map.get("finalAmount"); // 최종 결제 금액

        // 혜택 엔티티 조회
        BenefitEntity benefitEntity = null;
        Long usedBenefitId = null;
        if (map.get("benefitId") != null) {
            usedBenefitId = map.get("benefitId").longValue();
            benefitEntity = benefitRepository.findById(usedBenefitId)
                    .orElseThrow(() -> new RuntimeException("해당 혜택을 찾을 수 없습니다."));
        }

        // 할부 결제 - installmentMonth 값이 1보다 큰 경우 할부 결제로 처리
        int installmentMonth = request.getInstallmentMonth();
        if (installmentMonth > 1) {
            finalAmount = installmentPayment(card, installmentMonth, finalAmount);
            if (finalAmount < 0) { // 매달 결제 금액이 한도를 초과
                return new PosResponse(false, "할부 결제에 실패했습니다.", "INSTALLMENT_FAILED");
            }
        }

        // 결제 내역 생성
        PaymentEntity payment = PaymentEntity.builder()
                .originalAmount(request.getAmount())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .date(request.getPaymentDate())
                .status(installmentMonth > 1 ? PaymentStatus.PENDING : PaymentStatus.PAID)
                .installmentMonth(installmentMonth)
                .installmentRound(installmentMonth > 1 ? 1 : 0)
                .card(card)
                .store(store)
                .benefit(usedBenefitId != null ? benefitEntity : null)
                .build();
        paymentRepository.save(payment);

        // 월 사용금액 변경
        card.setMonthlySpent(card.getMonthlySpent() + finalAmount);
        cardRepository.save(card);

        return new PosResponse(true, "결제가 승인되었습니다.", "PAYMENT_APPROVED");
    }

    // 카드 유효성 검사
    private boolean validateCard(String cardNumber) {
        Optional<CardEntity> optionalCard = cardRepository.findByCardNumber(cardNumber);

        // 카드 존재 여부 검사
        if (optionalCard.isPresent()) {
            CardEntity card = optionalCard.get();

            // 카드 상태 검사
            if (card.getStatus() != CardStatus.ACTIVE) {
                return false;
            }

            // 카드 만료일 검사
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM");
            YearMonth expYearMonth = YearMonth.parse(card.getExpirationDate(), formatter); // 카드 만료일 YearMonth 객체로 변환
            YearMonth currentYearMonth = YearMonth.now(); // 현재 연월(YearMonth)

            // 만료일 비교
            if (expYearMonth.isBefore(currentYearMonth)) {
                return false;
            }
            return true; // 유효한 카드
        } else {
            return false;
        }
    }

    // 한도 검사
    private boolean isLimitExceeded(CardEntity card, int amount) {
        int monthlyAllowance = card.getMonthlyAllowance(); // 월 한도
        int monthlySpent = card.getMonthlySpent(); // 이번 달 누적 소비금액

        return (monthlySpent + amount) > monthlyAllowance;
    }

    // 혜택 적용 검사
    private Map<String, Integer> applyBenefit(List<BenefitEntity> myBenfitList, PosPaymentRequest request, String cardNumber) {
        Long storeId = request.getStoreId(); // 포스기로부터 전달받은 상점 ID
        int originAmount = request.getAmount(); // 결제 금액
        int finalAmount = originAmount; // 최종 결제 금액
        int discountAmount = 0;
        Integer usedBenefitId = null;
        Map<String, Integer> map = new HashMap<>();

        // 카드에 등록된 혜택 목록을 순회하면서 상점 ID가 일치하는 혜택 찾기
        for (BenefitEntity benefit : myBenfitList) {
            Long benefitId = benefit.getBenefitId();
            Optional<Long> storeIdOpt = benefitRepository.findStoreIdByBenefitId(benefit.getBenefitId());
            if (storeIdOpt.isPresent()) {
                Long benefitStoreId = storeIdOpt.get(); // 혜택이 적용된 상점 ID

                // 혜택 검사
                if (benefitStoreId.equals(storeId)) {
                    // 최소 결제 금액 검사
                    if (originAmount < benefit.getMinPayment()) {
                        break;
                    }

                    // 혜택 사용 가능 여부 검사
                    Optional<MyBenefitEntity> myBenefitOpt = myBenefitRepository.findMyBenefitByCardNumberAndBenefitId(cardNumber, benefitId);
                    if (myBenefitOpt.isPresent()) {
                        MyBenefitEntity myBenefitEntity = myBenefitOpt.get();

                        if (myBenefitEntity.getStatus() == MyBenefitStatus.WAITING) {
                            continue;
                        }

                        if (benefit.getUsageLimit() < myBenefitEntity.getUsedCount()) {
                            continue;
                        } else {
                            myBenefitEntity.setUsedCount(myBenefitEntity.getUsedCount() + 1); // 사용 횟수 증가
                        }
                    }

                    // 최대 할인 금액 검사
                    discountAmount = (int) (originAmount * (benefit.getDiscountRate() / 100.0));
                    if (discountAmount > benefit.getMaxDiscount()) {
                        discountAmount = benefit.getMaxDiscount();
                    }

                    // 혜택 적용 완료
                    usedBenefitId = benefitId.intValue();
                    map.put("benefitId", usedBenefitId);
                    finalAmount = originAmount - discountAmount;
                    break;
                }
            }
        }

        map.put("finalAmount", finalAmount);
        map.put("discountAmount", discountAmount);
        return map;
    }

    // 할부 결제
    private int installmentPayment(CardEntity card, int installmentMonth, int finalAmount) {
        // 최대 할부 개월 수 제한 (12개월)
        if (installmentMonth > 12) {
            return -1;
        }

        // 첫 회차 할부 결제 금액, 100원 단위 절사
        int installmentAmount = (finalAmount / installmentMonth) / 100 * 100;

        // 매달 결제 금액이 한도를 넘는지 검사
        int monthlyAllowance = card.getMonthlyAllowance();

        if (installmentAmount > monthlyAllowance) {
            return -1;
        } else {
            return installmentAmount;
        }
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    // 카드 환불 로직
    public PosResponse processRefund(PosRefundRequest request) {
        // 결제 조회
        Optional<PaymentEntity> paymentOpt = paymentRepository.findById(request.getPaymentId());
        if (paymentOpt.isEmpty()) {
            return new PosResponse(false, "결제내역을 찾을 수 없습니다.", "REFUND_DECLINED");
        }
        PaymentEntity payment = paymentOpt.get();

        // 카드 번호 비교
        if (payment.getCard() == null || !payment.getCard().getCardNumber().equals(request.getCardNumber())) {
            return new PosResponse(false, "카드가 일치하지 않습니다.", "REFUND_DECLINED");
        }

        // PaymentStatus 검사
        if (payment.getStatus() != PaymentStatus.PAID) {
            return new PosResponse(false, "환불 가능한 결제가 아닙니다.", "REFUND_DECLINED");
        }

        // 환불 내역 생성
        PaymentEntity refundPayment = PaymentEntity.builder()
                .originalAmount(payment.getOriginalAmount())
                .discountAmount(payment.getDiscountAmount())
                .finalAmount(payment.getFinalAmount())
                .date(LocalDateTime.now())
                .status(PaymentStatus.REFUNDED)
                .installmentMonth(0)
                .installmentRound(0)
                .card(payment.getCard())
                .store(payment.getStore())
                .benefit(payment.getBenefit())
                .build();
        paymentRepository.save(refundPayment);

        RefundEntity refund = RefundEntity.builder()
                .payment(payment)
                .amount(payment.getFinalAmount())
                .date(LocalDateTime.now())
                .build();

        refundRepository.save(refund);

        // 월 사용금액 변경
        CardEntity card = payment.getCard();
        card.setMonthlySpent(card.getMonthlySpent() - payment.getFinalAmount());
        cardRepository.save(card);

        return new PosResponse(true, "환불이 완료되었습니다.", "REFUND_APPROVED");
    }

    // 전체 기간 할인 금액 조회
    public BenefitStatementResponseDTO getTotalBenefit(Long userId) {
        Integer totalBenefit = paymentRepository.findTotalDiscountByUserId(userId);

        BenefitStatementResponseDTO dto = new BenefitStatementResponseDTO();
        dto.setTotalBenefit(totalBenefit);
        dto.setStatementList(null);
        dto.setTotalBenefitDiscount(null);
        dto.setUserName(null);

        return dto;
    }

}
