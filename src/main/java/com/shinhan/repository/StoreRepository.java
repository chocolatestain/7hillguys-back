package com.shinhan.repository;

import com.shinhan.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
        // 상점 ID로 상점엔티티 조회
        Optional<StoreEntity> findById(Long storeId);

}
