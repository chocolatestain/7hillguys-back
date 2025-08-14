package com.shinhan.peoch.invest.entity;

import com.shinhan.entity.UserProfileEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@Entity
@NoArgsConstructor @AllArgsConstructor
@Table(name = "user_profile_files")
public class UserProfileFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fileId;

    @OneToOne
    @JoinColumn(name = "user_profile_id", referencedColumnName = "userProfileId", nullable = false)
    private UserProfileEntity userProfile;

    @Column(nullable = false, length = 255)
    private String fileType;    //파일 타입(예: "image/png", "application/pdf")

    @Column(nullable = false)
    private Long fileSize;      //파일 크기(바이트 단위)

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
