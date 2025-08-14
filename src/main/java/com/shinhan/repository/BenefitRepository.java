package com.shinhan.repository;

import com.shinhan.entity.BenefitEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface BenefitRepository extends JpaRepository<BenefitEntity, Long> {
    // 모든 혜택 조회
    @NonNull
    List<BenefitEntity> findAll();

    // 특정 혜택이 적용되는 상점 ID 조회
    @Query("SELECT b.store.storeId FROM BenefitEntity b WHERE b.benefitId = :benefitId")
    Optional<Long> findStoreIdByBenefitId(@Param("benefitId") Long benefitId);
}
