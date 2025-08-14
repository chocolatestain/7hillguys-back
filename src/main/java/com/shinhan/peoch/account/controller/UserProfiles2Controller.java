package com.shinhan.peoch.account.controller;

import com.shinhan.entity.UserProfiles2Entity;
import com.shinhan.peoch.account.service.UserProfiles2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.shinhan.peoch.security.jwt.JwtTokenProvider;
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserProfiles2Controller {

    private final UserProfiles2Service userProfiles2Service;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/userprofiles2")
    public ResponseEntity<UserProfiles2Entity> getUserProfiles2(@CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return null;
        }
        Integer userId = userIdLong.intValue();

        UserProfiles2Entity profile = userProfiles2Service.getUserProfiles2ByUserId(userId);
        return ResponseEntity.ok(profile);
    }
}
