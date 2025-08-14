package com.shinhan.peoch.education.service;

import com.shinhan.entity.UserProfiles2Entity;
import com.shinhan.peoch.education.dto.UserProfile2DTO;
import com.shinhan.repository.UserProfile2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfile2Service {

    private final UserProfile2Repository userProfiles2Repository;

    public UserProfile2DTO getUserProfile(Long userId) {
        Optional<UserProfiles2Entity> entityOpt = userProfiles2Repository.findById(userId);

        if (entityOpt.isEmpty()) {
            throw new RuntimeException("해당 사용자의 능력치 정보가 없습니다.");
        }

        UserProfiles2Entity entity = entityOpt.get();

        // 엔티티 데이터를 DTO로 변환
        return convertToDTO(entity);
    }

    /**
     * 엔티티를 DTO로 변환하는 메서드
     */
    private UserProfile2DTO convertToDTO(UserProfiles2Entity entity) {
        return UserProfile2DTO.builder()
                .userId(entity.getUserId())
                .letter(entity.getLetter())
                .languageScore(entity.getLanguageScore())
                .certification(entity.getCertification())
                .internship(entity.getInternship())
                .grade(entity.getGrade())
                .build();
    }

    public UserProfile2DTO saveUserProfile(UserProfile2DTO dto) {
        // 만약 기존 레코드가 있다면 수정(업데이트)으로 처리됨
        UserProfiles2Entity entity = userProfiles2Repository.findById(dto.getUserId())
                .orElse(UserProfiles2Entity.builder()
                        .userId(dto.getUserId())
                        .build());
        entity.setLetter(dto.getLetter());
        entity.setLanguageScore(dto.getLanguageScore());
        entity.setCertification(dto.getCertification());
        entity.setInternship(dto.getInternship());
        entity.setGrade(dto.getGrade());
        userProfiles2Repository.save(entity);
        return convertToDTO(entity);
    }

    // userId를 기반으로 엔티티 삭제
    public void deleteUserProfile(Long userId) {
        userProfiles2Repository.deleteById(userId);
    }
}
