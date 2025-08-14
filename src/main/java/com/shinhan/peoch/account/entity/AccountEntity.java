package com.shinhan.peoch.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 계좌 ID

    @Column(nullable = false)
    private String bankName; // 은행명

    @Column(nullable = false, unique = true)
    private String accountNumber; // 계좌 번호

    @Column(nullable = false)
    private String accountHolder; // 예금주

    @Column(nullable = false)
    private BigDecimal balance; // 계좌 잔액

    @Column(nullable = false)
    private String accountType; // 계좌 유형 (예: 예금, 적금, 투자 등)

    private LocalDateTime createdAt; // 계좌 생성일
    private LocalDateTime updatedAt; // 마지막 업데이트 날짜

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
