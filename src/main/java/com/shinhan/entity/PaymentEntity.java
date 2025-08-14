package com.shinhan.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Integer originalAmount;

    private Integer discountAmount;

    private Integer finalAmount;

    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Integer installmentMonth;

    private Integer installmentRound;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "cardId")
    CardEntity card;

    @ManyToOne
    @JoinColumn(name = "storeId")
    StoreEntity store;

    @ManyToOne
    @JoinColumn(name = "benefitId")
    BenefitEntity benefit;

}
