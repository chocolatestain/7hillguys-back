package com.shinhan.repository;

import com.shinhan.entity.BenefitEntity;
import com.shinhan.entity.MyBenefitEntity;
import com.shinhan.entity.MyBenefitId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MyBenefitRepository extends JpaRepository<MyBenefitEntity, MyBenefitId> {
    // 특정 사용자가 현재 사용 중인 혜택 ID 목록 조회
    @Query("SELECT mb.benefit.benefitId FROM MyBenefitEntity mb WHERE mb.card.user.userId = :userId")
    List<Long> findBenefitIdsByUserId(@Param("userId") Long userId);


    // BenefitEntity에 card 연관관계가 있고, CardEntity의 cardId를 기준으로 조회
    List<MyBenefitEntity> findAppliedByCard_CardId(Long cardId);

    // 유저 ID로 사용중인 혜택 엔티티 목록 조회
    @Query("select mb.benefit from MyBenefitEntity mb where mb.card.user.userId = :userId")
    List<BenefitEntity> findBenefitsByUserId(@Param("userId") Long userId);

    // 카드번호로 사용중인 혜택 엔티티 목록 조회
    @Query("SELECT mb.benefit FROM MyBenefitEntity mb WHERE mb.card.cardNumber = :cardNumber")
    List<BenefitEntity> findBenefitsByCardNumber(@Param("cardNumber") String cardNumber);

    // 카드번호와 혜택 ID로 특정 혜택 엔티티 조회
    @Query("SELECT mb FROM MyBenefitEntity mb WHERE mb.card.cardNumber = :cardNumber AND mb.benefit.benefitId = :benefitId")
    Optional<MyBenefitEntity> findMyBenefitByCardNumberAndBenefitId(@Param("cardNumber") String cardNumber, @Param("benefitId") Long benefitId);

    @Query("select m from MyBenefitEntity m join fetch m.benefit where m.card.cardId = :cardId")
    List<MyBenefitEntity> findAppliedByCardIdWithBenefit(@Param("cardId") Long cardId);

}
