package com.shinhan.peoch.benefit.service;


import com.shinhan.entity.*;
import com.shinhan.peoch.benefit.dto.BenefitApplyDTO;
import com.shinhan.peoch.benefit.dto.BenefitResponseDTO;
import com.shinhan.peoch.benefit.dto.MyBenefitDTO;
import com.shinhan.repository.BenefitRepository;
import com.shinhan.repository.CardRepository;
import com.shinhan.repository.MyBenefitRepository;
import com.shinhan.repository.PaymentRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyBenefitService {

    private final MyBenefitRepository myBenefitRepository;
    private final BenefitRepository benefitRepository;
    private final CardRepository cardRepository;
    private final EntityManager entityManager;
    private final PaymentRepository paymentRepository;


    // 카드ID에 따라 혜택 데이터를 조회하는 예시 (실제 구현은 카드와 혜택의 관계에 따라 달라짐)
    @Transactional(readOnly = true)
    public BenefitResponseDTO getBenefitsByUserId(Long userId) {
        // 내 카드 정보를 사용자 ID로 조회 (CardEntity에 user 연관관계가 있다고 가정)
        CardEntity card = cardRepository.findByUser_UserId(userId);
         //       .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다."));

        // 카드에 적용된 혜택 조회 (BenefitEntity에 card 관계가 있고, 메서드 이름은 findAppliedByCard_CardId 사용)
        List<MyBenefitEntity> appliedBenefits = myBenefitRepository.findAppliedByCard_CardId(card.getCardId());
        // 전체 사용 가능한 혜택 조회 (사용자 기준, 필요에 따라 사용자 ID로 조회하는 메서드)
        List<BenefitEntity> availableBenefits = benefitRepository.findAll();

        return new BenefitResponseDTO(card, appliedBenefits, availableBenefits);
    }

    @Transactional
    public MyBenefitDTO deleteBenefit(Long benefitId, Long cardId) {
        MyBenefitId id = new MyBenefitId(benefitId, cardId);
        MyBenefitEntity entity = myBenefitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 카드에 적용된 혜택이 존재하지 않습니다."));

        // 삭제 전에 필요한 정보를 임시로 저장
        // benefitId만 반환할 경우, 오버로딩 생성자를 사용할 수 있습니다.
        MyBenefitDTO dto = new MyBenefitDTO(benefitId);

        myBenefitRepository.delete(entity);

        return dto;
    }

    // 결제 시 혜택 적용 (임시 추가된 혜택을 카드에 병합하는 로직)
    @Transactional
    public List<MyBenefitDTO> applyBenefits(BenefitApplyDTO dto) {
        CardEntity cardProxy = entityManager.getReference(CardEntity.class, dto.getCardId());
        List<MyBenefitEntity> savedEntities = new ArrayList<>();

        for (Long benefitId : dto.getBenefitIds()) {
            BenefitEntity benefitProxy = entityManager.getReference(BenefitEntity.class, benefitId);

            MyBenefitId myBenefitId = new MyBenefitId(benefitId, dto.getCardId());
            MyBenefitEntity myBenefitEntity = MyBenefitEntity.builder()
                    .myBenefitId(myBenefitId)
                    .usedCount(0)
                    .status(MyBenefitStatus.ACTIVE)
                    .card(cardProxy)
                    .benefit(benefitProxy)
                    .build();

            MyBenefitEntity savedEntity = myBenefitRepository.save(myBenefitEntity);
            savedEntities.add(savedEntity);
        }

        // 저장된 데이터를 조회해 필요한 연관 데이터를 로드하거나 fetch join 사용
        // 혹은 savedEntities 리스트를 바로 DTO로 변환할 수 있습니다.
        List<MyBenefitDTO> dtos = savedEntities.stream()
                .map(entity -> new MyBenefitDTO(
                        entity.getMyBenefitId().getBenefitId(),
                        entity.getBenefit().getName(),
                        entity.getBenefit().getDescription(),
                        entity.getBenefit().getFee(),
                        entity.getStatus(),
                        entity.getUsedCount(),
                        entity.getBenefit().getDiscountRate()
                ))
                .collect(Collectors.toList());

        return dtos;
    }
    /**
     * 선택한 카드(cardId)와 월(예: "2025-03")에 해당하는 결제 내역을 조회합니다.
     *
     * @param cardId 카드 아이디
     * @param month  "yyyy-MM" 형식의 문자열
     * @return 해당 카드의 월별 결제 내역 리스트
     */
    @Transactional(readOnly = true)
    public List<PaymentEntity> getPaymentsByCardAndMonth(Long cardId, String month) {
        LocalDate startLocalDate = LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime startDateTime = startLocalDate.atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusMonths(1).minusSeconds(1);
        return paymentRepository.findByCard_CardIdAndDateBetween(cardId, startDateTime, endDateTime);
    }



}

