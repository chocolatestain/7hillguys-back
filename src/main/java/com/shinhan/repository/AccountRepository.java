package com.shinhan.repository;

import com.shinhan.peoch.account.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    // user테이블로 조회
    Optional<AccountEntity> findByUserId(Long userId);
    
    // 계좌 번호로 계좌 조회
    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    // 특정 은행의 모든 계좌 조회
    List<AccountEntity> findByBankName(String bankName);

    // 예금주 이름으로 계좌 조회
    List<AccountEntity> findByAccountHolder(String accountHolder);

}
