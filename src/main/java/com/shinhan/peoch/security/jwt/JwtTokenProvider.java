package com.shinhan.peoch.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);  // `Keys.hmacShaKeyFor()` 사용
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.info("[JwtTokenProvider] Claims 내용: {}", claims);

            Integer userId = claims.get("userId", Integer.class);
            if (userId == null) {
                log.error("[JwtTokenProvider] JWT에서 userId를 추출할 수 없음! Claims: {}", claims);
                return null;
            }

            log.info("[JwtTokenProvider] 추출된 userId: {}", userId);
            return userId.longValue();
        } catch (Exception e) {
            log.error("[JwtTokenProvider] JWT 파싱 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}