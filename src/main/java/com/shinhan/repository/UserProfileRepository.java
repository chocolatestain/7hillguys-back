package com.shinhan.repository;

import com.shinhan.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
 
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Integer> {
    Optional<UserProfileEntity> findByUserId(Integer userId);
    //제일 최신 profile
    Optional<UserProfileEntity> findFirstByUserIdOrderByUpdatedAtDesc(Integer userId);
    //제일 과거 profile
    Optional<UserProfileEntity> findFirstByUserIdOrderByUpdatedAtAsc(Integer userId);
    UserProfileEntity findByUserProfileId(Integer userProfileId);
    @Query("SELECT up FROM UserProfileEntity up WHERE up.userId = :userId " +
            "AND EXISTS (SELECT ei FROM ExpectedIncomeEntity ei WHERE ei.userProfile = up) " +
            "ORDER BY up.updatedAt DESC LIMIT 1")
    Optional<UserProfileEntity> findLatestProfileWithExpectedIncome(@Param("userId") Integer userId);


}
