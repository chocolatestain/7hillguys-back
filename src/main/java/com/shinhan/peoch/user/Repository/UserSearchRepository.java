package com.shinhan.peoch.user.Repository;
 
import com.shinhan.peoch.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 

public interface UserSearchRepository extends JpaRepository<UserEntity, Long> {
  
 List<UserEntity> findByNameContaining(String name);


}
