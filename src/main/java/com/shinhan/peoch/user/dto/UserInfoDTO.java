package com.shinhan.peoch.user.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserInfoDTO {
    // 사용자 기본정보 (UserEntity)
    private Long userId;
    private String name;
    private String email;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private String role;
    private LocalDateTime createdAt;
    
    // 투자 정보 (InvestmentEntity)
    private Integer grantId;
    private String expectedIncome;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long originalInvestValue;
    private Integer monthlyAllowance;
    private Double refundRate;
    private Integer maxInvestment;
    private String field;
    private Long investValue;
    private Integer tempAllowance;
    private LocalDateTime investmentCreatedAt;
    
    // 사용자 프로필 정보 (UserProfileEntity)
    private Integer userProfileId;
    private String universityInfo;
    private String studentCard;
    private String certification;
    private String familyStatus;
    private Long assets;
    private Boolean criminalRecord;
    private Integer healthStatus;
    private Boolean gender;
    private String profileAddress;
    private Integer mentalStatus;
    private LocalDateTime profileCreatedAt;
    private LocalDateTime profileUpdatedAt;
}
