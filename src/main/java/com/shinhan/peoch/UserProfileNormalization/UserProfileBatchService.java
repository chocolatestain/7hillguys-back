package com.shinhan.peoch.UserProfileNormalization;

import com.shinhan.entity.NormUserProfilesEntity;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserProfileBatchService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserProfileNormalizationService normalizationService;

    public List<NormUserProfilesEntity> normalizeAllUserProfiles() {
        List<UserProfileEntity> allProfiles = userProfileRepository.findAll();
        List<NormUserProfilesEntity> normalizedProfiles = new ArrayList<>();

        for (UserProfileEntity profile : allProfiles) {
            try {
                NormUserProfilesEntity normalizedProfile = normalizationService.normalizeAndSaveUserProfile(profile.getUserProfileId());
                normalizedProfiles.add(normalizedProfile);
            } catch (Exception e) {
                // 오류 로깅 및 다음 프로필 처리 계속
                System.err.println("Error normalizing profile ID " + profile.getUserProfileId() + ": " + e.getMessage());
            }
        }

        return normalizedProfiles;
    }
}
