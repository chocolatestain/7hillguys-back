package com.shinhan.peoch.account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_salary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSalaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "income_id")
    private Long incomeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "salary", precision = 15, scale = 2)
    private Integer salary;

    @Column(name = "year")
    private Integer year;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
