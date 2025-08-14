package com.shinhan.peoch.account.service;

import com.shinhan.entity.UserProfiles2Entity;
import com.shinhan.repository.UserProfiles2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfiles2Service {

    private final UserProfiles2Repository userProfiles2Repository;

    public UserProfiles2Entity getUserProfiles2ByUserId(Integer userId) {
        return userProfiles2Repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저의 프로필 정보가 없습니다. userId: " + userId));
    }
}
