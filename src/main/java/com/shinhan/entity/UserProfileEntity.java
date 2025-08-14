// UserProfileEntity
package com.shinhan.entity;

import com.shinhan.peoch.invest.entity.UserProfileFileEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@ToString(exclude = "normUserProfile")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userProfileId;  //PK

    @Column(name = "user_id")
    private Integer userId;

    @Column(columnDefinition = "JSON")
    private String universityInfo;      //JSON데이터: 학교, 학과

    @Column(columnDefinition = "JSON")
    private String studentCard;         //JSON데이터: 고등학교, 내신성적

    @Column(columnDefinition = "JSON")
    private String certification;       //JSON데이터: 자격증명 0개 이상

    @Column(columnDefinition = "JSON")
    private String familyStatus;        //JSON데이터: 결혼여부(0:미혼, 1:결혼), 자녀수

    private Long assets;                //자산

    private Boolean criminalRecord;     //범죄여부기록(0:범죄기록 없음, 1:범죄기록 있음)

    private Integer healthStatus;       //건강점검 데이터 점수 (1~100)

    private Boolean gender;             //성별(0:남자, 1:여자)

    @Column(length = 255)
    private String address;

    private Integer mentalStatus;       //정신건강 점수

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfileFileEntity file;

    @OneToOne(mappedBy = "userProfile")
    private NormUserProfilesEntity normUserProfile;
}
