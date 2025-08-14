package com.shinhan.repository;

import com.shinhan.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository <CardEntity, Long> {
    // 특정 사용자의 카드 조회
    CardEntity findByUser_UserId(Long userId);

    // 카드번호로 카드 조회
    Optional<CardEntity> findByCardNumber(String cardNumber);

    // 카드번호로 카드 소유자의 userId 찾기
    @Query("SELECT c.user.userId FROM CardEntity c WHERE c.cardNumber = :cardNumber")
    Long findUserIdByCardNumber(@Param("cardNumber") String cardNumber);

}
