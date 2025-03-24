package com.shinhan.peoch.invest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.peoch.invest.dto.UserProfileDTO;
import com.shinhan.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;

    //사용자 프로필 저장
    @Transactional
    public UserProfileEntity saveUserProfile(UserProfileDTO dto) {
        try {
            UserProfileEntity userProfileEntity = UserProfileEntity.builder()
                    .userId(dto.getUserId())
                    .universityInfo(objectMapper.writeValueAsString(dto.getUniversityInfo()))  // JSON 변환
                    .studentCard(objectMapper.writeValueAsString(dto.getStudentCard()))
                    .certification(objectMapper.writeValueAsString(dto.getCertification()))
                    .familyStatus(objectMapper.writeValueAsString(dto.getFamilyStatus()))
                    .assets(dto.getAssets())
                    .criminalRecord(dto.getCriminalRecord())
                    .healthStatus(dto.getHealthStatus())
                    .gender(dto.getGender())
                    .address(dto.getAddress())
                    .mentalStatus(dto.getMentalStatus())
                    .build();

            return userProfileRepository.save(userProfileEntity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류 발생", e);
        }
    }

    public UserProfileEntity findUserProfileByUserIdOrderByUpdatedAtDesc(Integer userId) {
        // Optional을 사용하여 userId로 가장 최신 사용자프로필 검색
        return userProfileRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 Profile이 존재하지 않습니다."));
    }
}