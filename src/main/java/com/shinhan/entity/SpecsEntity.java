package com.shinhan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "specs")
public class SpecsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer specsId;

    @Column(columnDefinition = "JSON")
    private String department;

    @Column(columnDefinition = "JSON")
    private String grade;

    @Column(name = "language_score", columnDefinition = "JSON")
    private String languageScore;

    @Column(columnDefinition = "JSON")
    private String internship;

    @Column(columnDefinition = "JSON")
    private String letter;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer companyId; // 외래 키로 설정 가능
}
