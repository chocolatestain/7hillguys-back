package com.shinhan.repository;

import com.shinhan.entity.InvestmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvestmentRepository extends JpaRepository<InvestmentEntity, Integer> {
    Optional<InvestmentEntity> findByUserId(Integer userId);
    // 최신 updatedAt 기준으로 가장 최근 1개 엔티티 조회
    Optional<InvestmentEntity>  findFirstByUserIdOrderByUpdatedAtDesc(long userId);
    // CreatedAt 기준으로 가장 과거 1개 엔티티 조회
    InvestmentEntity findFirstByUserIdOrderByCreatedAtAsc(long userId);
    //사용자 ID로 투자자 조회
    InvestmentEntity findInvestmentByUserId(long userId);
}
