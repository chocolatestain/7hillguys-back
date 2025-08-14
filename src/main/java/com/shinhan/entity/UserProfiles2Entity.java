package com.shinhan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "user_profiles2")
public class UserProfiles2Entity {
    @Id
    private Long userId; // Users 테이블과의 외래 키 관계

    @Column(columnDefinition = "JSON")
    private String letter;

    @Column(name = "language_score", columnDefinition = "JSON")
    private String languageScore;

    @Column(columnDefinition = "JSON")
    private String certification;

    @Column(columnDefinition = "JSON")
    private String internship;

    @Column(columnDefinition = "JSON")
    private String grade;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
