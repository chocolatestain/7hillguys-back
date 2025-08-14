package com.shinhan.peoch.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment_charge")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentChargeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // UserSalary와의 Many-to-One 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_id", nullable = false)
    private UserSalaryEntity userSalary;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "invest_charge_amount", precision = 15, scale = 2)
    private Integer investChargeAmount;

    @Column(name = "invest_payment_amount", precision = 15, scale = 2)
    private Integer investPaymentAmount;

    @Column(name = "invest_payment_status", length = 50)
    private String investPaymentStatus;
}
