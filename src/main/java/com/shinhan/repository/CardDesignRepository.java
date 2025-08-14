package com.shinhan.repository;

import com.shinhan.entity.CardDesignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CardDesignRepository extends JpaRepository<CardDesignEntity, Integer> {
}
