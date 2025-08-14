package com.shinhan.repository;

import com.shinhan.entity.UserProfiles2Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfile2Repository extends JpaRepository<UserProfiles2Entity, Long> {

}