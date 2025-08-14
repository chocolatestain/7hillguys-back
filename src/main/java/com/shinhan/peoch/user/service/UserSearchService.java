package com.shinhan.peoch.user.service;

import com.shinhan.entity.InvestmentEntity;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.user.Repository.UserSearchRepository;
import com.shinhan.peoch.user.dto.UserInfoDTO;
import com.shinhan.repository.UserProfileRepository;
import com.shinhan.repository.InvestmentRepository;
import com.shinhan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import java.util.List;
import java.util.Optional;

@Service
public class UserSearchService {

    @Autowired
    private UserSearchRepository UserSearchRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InvestmentRepository investmentRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    public List<UserEntity> searchUsersByName(String query) { 
        return UserSearchRepository.findByNameContaining(query);
    }

    public UserInfoDTO getAdminUserFlatDetail(Long userId) {
        // 사용자 기본정보 조회 (UserEntity)
        UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다."));
        
        // 투자 정보 조회 (InvestmentEntity)
        System.out.println("userId :" + userId.intValue());
        Optional<InvestmentEntity> investmentOpt = investmentRepository.findByUserId(userId.intValue());
        System.out.println("InvestmentOp" + investmentOpt);
        // 사용자 프로필 정보 조회 (UserProfileEntity)
        UserProfileEntity profileOpt = userProfileRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId.intValue()).orElse(null);
        
        UserInfoDTO dto = new UserInfoDTO();
        // 사용자 기본정보 세팅
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBirthDate(user.getBirthdate());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole().toString());
        dto.setCreatedAt(user.getCreatedAt());
        
        // 투자 정보 세팅
        if(investmentOpt.isPresent()){
            InvestmentEntity inv = investmentOpt.get();
            dto.setGrantId(inv.getGrantId());
            dto.setExpectedIncome(inv.getExpectedIncome());
            dto.setStartDate(inv.getStartDate());
            dto.setEndDate(inv.getEndDate());
            dto.setStatus(inv.getStatus().toString());
            dto.setOriginalInvestValue(inv.getOriginalInvestValue());
            dto.setMonthlyAllowance(inv.getMonthlyAllowance());
            dto.setRefundRate(inv.getRefundRate());
            dto.setMaxInvestment(inv.getMaxInvestment());
            dto.setField(inv.getField());
            dto.setInvestValue(inv.getInvestValue());
            dto.setTempAllowance(inv.getTempAllowance());
            dto.setInvestmentCreatedAt(inv.getCreatedAt());
        }
        
        // 사용자 프로필 정보 세팅
        if(profileOpt != null){
            UserProfileEntity prof = profileOpt;
            dto.setUserProfileId(prof.getUserProfileId());
            dto.setUniversityInfo(prof.getUniversityInfo());
            dto.setStudentCard(prof.getStudentCard());
            dto.setCertification(prof.getCertification());
            dto.setFamilyStatus(prof.getFamilyStatus());
            dto.setAssets(prof.getAssets());
            dto.setCriminalRecord(prof.getCriminalRecord());
            dto.setHealthStatus(prof.getHealthStatus());
            dto.setGender(prof.getGender());
            dto.setProfileAddress(prof.getAddress());
            dto.setMentalStatus(prof.getMentalStatus());
            dto.setProfileCreatedAt(prof.getCreatedAt());
            dto.setProfileUpdatedAt(prof.getUpdatedAt());
        }
        
        return dto;
    }

    
}
