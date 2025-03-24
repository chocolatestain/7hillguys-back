package com.shinhan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
@ToString(exclude = "userProfile") // userProfile을 toString()에서 제외
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity

@Table(name = "expected_income")
public class ExpectedIncomeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer grantId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_profile_id", referencedColumnName = "userProfileId")
    private UserProfileEntity userProfile;

    @Column(columnDefinition = "JSON", nullable = false)
    private String expectedIncome;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}


