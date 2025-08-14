package com.shinhan.repository;

import com.shinhan.peoch.account.entity.InvestmentChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface BillRepository extends JpaRepository<InvestmentChargeEntity, Long> {
    Optional<InvestmentChargeEntity> findByUserSalary_UserId(Long userId);
}

