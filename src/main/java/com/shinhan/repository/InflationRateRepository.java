package com.shinhan.repository;

import com.shinhan.entity.InflationRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InflationRateRepository extends JpaRepository<InflationRateEntity, Integer> {
    InflationRateEntity findByYear(Integer year);
}
