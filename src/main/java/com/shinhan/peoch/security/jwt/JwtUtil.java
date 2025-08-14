package com.shinhan.peoch.security.jwt;

import com.shinhan.peoch.auth.entity.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
    private final Key key;
    private final long accessTokenExpTime;

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration_time}") long accessTokenExpTime
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpTime = accessTokenExpTime;
    }

    public String createAccessToken(UserEntity user) {
        return createToken(user, accessTokenExpTime);
    }

    private String createToken(UserEntity user, long expireTime) {
        Claims claims = Jwts.claims();
        claims.put("userId", user.getUserId());
        claims.put("userName", user.getName());
        claims.put("userEmail", user.getEmail());
        claims.put("userRole", user.getRole());

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(expireTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserEmail(String token) {
        Claims claims = parseClaims(token);
        String email = claims.get("userEmail", String.class);
        if (email == null) {
            log.error("JWT에서 userEmail을 추출할 수 없음! Claims: {}", claims);
        }
        return email;
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        log.info("[JwtUtil] Claims 내용: {}", claims); // Claims 값 로그 추가

        Integer userId = claims.get("userId", Integer.class);
        if (userId == null) {
            log.error("[JwtUtil] JWT에서 userId를 추출할 수 없음! Claims: {}", claims);
            return null;
        }
        log.info("[JwtUtil] 추출된 userId: {}", userId);
        return userId.longValue();
    }

    public boolean validationToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public long getExpirationTime(String token) {
        return parseClaims(token).getExpiration().getTime() - System.currentTimeMillis();
    }
}