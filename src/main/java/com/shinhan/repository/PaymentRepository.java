package com.shinhan.repository;

import com.shinhan.entity.PaymentEntity;
import com.shinhan.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    // 특정 사용자, 특정 기간 결제 내역 조회
    List<PaymentEntity> findByCard_User_UserIdAndDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // 특정 사용자, 특정 상점, 특정 기간 결제 내역 조회
    List<PaymentEntity> findByCard_User_UserIdAndStore_StoreIdAndDateBetween(Long userId, Long storeId, LocalDateTime start, LocalDateTime end);

    // 해당 userId 사용자의 discountAmount 총합을 Optional<Long>으로 리턴
    @Query("SELECT COALESCE(SUM(p.discountAmount), 0) FROM PaymentEntity p WHERE p.card.user.userId = :userId")
    Optional<Long> sumDiscountAmountByUserId(@Param("userId") long userId);

    // userId에 해당되는 것들 중에 pending+paid 상태인 payment의  finalAmount 총합을 Optional<Long>으로 리턴
    @Query("SELECT COALESCE(SUM(p.finalAmount), 0) FROM PaymentEntity p " +
            "WHERE p.card.user.userId = :userId " +
            "AND (p.status = 'PAID' OR p.status = 'PENDING')")
    Optional<Long> sumFinalAmountByUserId(@Param("userId") long userId);

    // 특정 사용자와 기간에 대해 결제 내역의 finalAmount 합계를 반환
    @Query("SELECT COALESCE(SUM(p.finalAmount), 0) " +
            "FROM PaymentEntity p " +
            "WHERE p.card.user.userId = :userId " +
            "AND p.date BETWEEN :startDate AND :endDate " +
            "AND p.status IN ('PAID', 'PENDING')")
    Integer findTotalFinalAmountByUserAndDateBetween(@Param("userId") Long userId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // 특정 사용자와 같은 나이인 사용자들의 특정 기간에 대해 평균 결제 금액 반환
    @Query("SELECT AVG(p.finalAmount) " +
            "FROM PaymentEntity p " +
            "JOIN p.card c " +
            "JOIN c.user u " +
            "WHERE u.birthdate BETWEEN :birthDateStart AND :birthDateEnd " +
            "AND p.date BETWEEN :startDate AND :endDate")
    Double findAverageTotalFinalAmountByBirthdateBetween(
            @Param("birthDateStart") java.time.LocalDate birthDateStart,
            @Param("birthDateEnd") java.time.LocalDate birthDateEnd,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // userId의 모든 결제 내역 조회(paid+pending)
    List<PaymentEntity> findByCard_User_UserId(Long userId);

    @Query("SELECT FUNCTION('DATE_FORMAT', p.date, '%Y-%m') as month, SUM(p.finalAmount) as total " +
            "FROM PaymentEntity p " +
            "WHERE p.card.user.userId = :userId " +
            "AND p.date BETWEEN :startDate AND :endDate " +
            "AND (p.status = 'PAID' OR p.status = 'PENDING') " +
            "GROUP BY FUNCTION('DATE_FORMAT', p.date, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', p.date, '%Y-%m') ASC")
    List<Object[]> findMonthlyPaymentsByUserIdAndDateBetweenAndStatus(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);



    List<PaymentEntity> findByCard_CardIdAndDateBetween(Long cardId, LocalDateTime start, LocalDateTime end);




    List<PaymentEntity> findByStatus(PaymentStatus paymentStatus);

    @Query("SELECT SUM(p.discountAmount) " +
            "FROM PaymentEntity p " +
            "JOIN p.card c " +
            "JOIN c.user u " +
            "WHERE u.userId = :userId AND p.discountAmount > 0")
    Integer findTotalDiscountByUserId(@Param("userId") Long userId);



}
