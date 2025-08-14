package com.shinhan.repository;

import com.shinhan.peoch.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u.name FROM UserEntity u WHERE u.userId = :userId")
    Optional<String> findNameById(@Param("userId") Long userId);
}
