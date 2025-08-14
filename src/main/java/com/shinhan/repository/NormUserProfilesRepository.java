package com.shinhan.repository;

import com.shinhan.entity.NormUserProfilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NormUserProfilesRepository extends JpaRepository<NormUserProfilesEntity, Integer> {
}
