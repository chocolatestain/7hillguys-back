package com.shinhan.peoch.UserProfileNormalization;

import com.shinhan.entity.NormUserProfilesEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/normalization")
public class UserNormalizeProfileController {
    private final UserProfileBatchService userProfileBatchService;

    @PostMapping("/normalize-all")
    public ResponseEntity<List<NormUserProfilesEntity>> normalizeAllUserProfiles() {
        try {
            List<NormUserProfilesEntity> normalizedProfiles = userProfileBatchService.normalizeAllUserProfiles();
            return ResponseEntity.ok(normalizedProfiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
