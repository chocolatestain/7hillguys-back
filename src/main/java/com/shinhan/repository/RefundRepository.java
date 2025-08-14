package com.shinhan.repository;

import com.shinhan.entity.RefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface RefundRepository extends JpaRepository<RefundEntity, Long> {
    List<RefundEntity> findByPayment_Card_User_UserIdAndDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
