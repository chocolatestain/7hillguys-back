package com.shinhan.repository;

import com.shinhan.entity.UserProfiles2Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfiles2Repository extends JpaRepository<UserProfiles2Entity, Integer> {
    Optional<UserProfiles2Entity> findByUserId(Integer userId);
}
