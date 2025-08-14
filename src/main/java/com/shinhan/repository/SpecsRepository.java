package com.shinhan.repository;

import com.shinhan.entity.SpecsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecsRepository extends JpaRepository<SpecsEntity, Integer> {
}
