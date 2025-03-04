package com.shinhan.peoch.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final StringRedisTemplate redisTemplate;
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60; //7일

    //Refresh Token 저장
    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(email, refreshToken, REFRESH_TOKEN_EXPIRATION, TimeUnit.SECONDS);
    }

    //Refresh Token 조회
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    //Refresh Token 삭제 (로그아웃 시)
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(email);
    }
}