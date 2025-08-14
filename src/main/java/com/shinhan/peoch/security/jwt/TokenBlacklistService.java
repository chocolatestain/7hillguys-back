package com.shinhan.peoch.security.jwt;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TokenBlacklistService {
    // 토큰을 key, 토큰 만료 시각(시스템 시간 + 남은 시간)을 value로 저장
    private final ConcurrentMap<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expirationTime) {
        // 현재 시각을 기준으로 남은 시간을 더해 만료 시각을 저장
        blacklist.put(token, System.currentTimeMillis() + expirationTime);
    }

    public boolean isTokenBlacklisted(String token) {
        Long expTime = blacklist.get(token);
        if (expTime == null) {
            return false;
        }
        // 토큰의 만료 시간이 지났으면 블랙리스트에서 제거
        if (System.currentTimeMillis() > expTime) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
