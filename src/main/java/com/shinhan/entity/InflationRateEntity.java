package com.shinhan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "inflation_rate")
public class InflationRateEntity {
    @Id
    private Integer year; // 연도를 기본 키로 사용


    @Column(columnDefinition = "JSON")
    private String inflationRate; // JSON 형식으로 저장된 물가 상승률 데이터
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
