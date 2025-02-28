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
                .setClaims(claims) //정보 저장
                .setIssuedAt(Date.from(now.toInstant()))	//발급 시간
                .setExpiration(Date.from(tokenValidity.toInstant())) // set 만료시간
                .signWith(key, SignatureAlgorithm.HS256) // 사용할 암호화 알고리즘과 signature 에 들어갈 secret값 세팅
                .compact();
    }

    public String getUserEmail(String token) {
        Claims claims = parseClaims(token);
        String email = claims.get("userEmail", String.class);

        if (email == null) {
            log.error("JWT에서 userEmail을 추출할 수 없음! Claims: {}", claims);
        }
        return email;
//        return parseClaims(token).get("userEmail", String.class);
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

    //JWT 남은 만료 시간 가져오는 메서드
    public long getExpirationTime(String token) {
        return parseClaims(token).getExpiration().getTime() - System.currentTimeMillis();
    }
}
