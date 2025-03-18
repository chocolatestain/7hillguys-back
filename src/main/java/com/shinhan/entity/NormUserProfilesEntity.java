package com.shinhan.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@ToString(exclude = "userProfile")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "norm_user_profiles")
public class NormUserProfilesEntity {

    @Id
    private Integer userProfileId; // PK & FK

    private Integer university;

    @Column(name = "education_major")
    private Integer educationMajor;

    private Integer certification;

    @Column(name = "family_status")
    private Integer familyStatus;

    private Integer assets;

    @Column(name = "criminal_record")
    private Integer criminalRecord;

    @Column(name = "health_status")
    private Integer healthStatus;

    private Boolean gender;

    private Integer address;

    @Column(name = "mental_status")
    private Integer mentalStatus;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne
    @MapsId // 이 필드를 PK로 사용
    @JoinColumn(name = "user_profile_id", referencedColumnName = "userProfileId")
    private UserProfileEntity userProfile;
}
