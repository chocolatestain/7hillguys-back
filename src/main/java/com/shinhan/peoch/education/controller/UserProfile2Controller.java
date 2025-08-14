package com.shinhan.peoch.education.controller;

import com.shinhan.peoch.education.dto.UserProfile2DTO;
import com.shinhan.peoch.education.service.UserProfile2Service;
import com.shinhan.peoch.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/myspecs")
@RequiredArgsConstructor
public class UserProfile2Controller {

    @Autowired
    private final UserProfile2Service userProfile2Service;


    @GetMapping
    public ResponseEntity<UserProfile2DTO> getUserSpec(@AuthenticationPrincipal SecurityUser securityUser) {
        Long userId = securityUser.getUserId();
        UserProfile2DTO userProfile2DTO = userProfile2Service.getUserProfile(userId);
        return ResponseEntity.ok(userProfile2DTO);
    }

    @PostMapping
    public ResponseEntity<UserProfile2DTO> saveUserProfile(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody UserProfile2DTO dto
    ) {
        // 토큰에서 가져온 사용자 ID로 설정
        dto.setUserId(securityUser.getUserId());
        // 이미 프로필이 존재하면 추가 저장 불가 처리
        if (userProfile2Service.getUserProfile(dto.getUserId()) != null) {
            return ResponseEntity.badRequest().body(null);
        }
        UserProfile2DTO createdDto = userProfile2Service.saveUserProfile(dto);
        return ResponseEntity.ok(createdDto);
    }

    @PutMapping
    public ResponseEntity<UserProfile2DTO> updateUserProfile(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody UserProfile2DTO dto
    ) {
        // 토큰에서 가져온 사용자 ID로 업데이트
        dto.setUserId(securityUser.getUserId());
        UserProfile2DTO updatedDto = userProfile2Service.saveUserProfile(dto);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUserProfile(
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        userProfile2Service.deleteUserProfile(securityUser.getUserId());
        return ResponseEntity.ok().build();
    }




}