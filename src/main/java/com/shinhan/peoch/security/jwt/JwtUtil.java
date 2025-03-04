package com.shinhan.peoch.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    //[1] 외부 설정값 주입
    //"jwt.secret" 키로부터 base64 인코딩된 문자열을 받아옴
    private final String secret;

    //[2] 실제 시크릿 키(디코딩 결과)를 보관할 필드
    private final SecretKey key;

    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; //15분
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; //7일

    //[3] 생성자 주입
    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        // base64 디코딩 후 Key 객체 생성
        byte[] decodedKey = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(decodedKey);
        this.secret = secretKey;
    }

    //Access Token 생성
    public String generateAccessToken(String email, String role) {
        return Jwts.builder()
                .setClaims(createClaims(email, role))
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();
    }

    //Refresh Token 생성
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();
    }

    //JWT에서 이메일 및 역할을 담을 Claims 생성
    private Map<String, Object> createClaims(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        return claims;
    }

    //토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    //토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    //토큰 만료 시간 가져오기
    public long getExpirationTime(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .getTime();
    }
}