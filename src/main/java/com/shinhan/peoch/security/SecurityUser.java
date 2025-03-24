package com.shinhan.peoch.security;
import java.time.LocalDate;
import java.util.ArrayList; // 추가
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.security.jwt.JwtUtil;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class SecurityUser extends User {
    private static final String ROLE_PREFIX = "ROLE_";
    private final UserEntity user;
    private final JwtUtil jwtUtil; // 추가
    public SecurityUser(UserEntity user, JwtUtil jwtUtil) { // JwtUtil 주입
        super(user.getEmail(), user.getPassword(), makeRole(user));
        this.user = user;
        this.jwtUtil = jwtUtil; // 추가
    }
    public Long getUserId() {
        return user.getUserId();
    }
    public String getName() {
        return user.getName();
    }
    public LocalDate getBirthdate() {
        return user.getBirthdate();
    }
    private static Collection<? extends GrantedAuthority> makeRole(UserEntity user) {
        Collection<GrantedAuthority> roleList = new ArrayList<>();
        roleList.add(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole()));
        return roleList;
    }
    public Long getUserId(String token) {
        Claims claims = jwtUtil.parseClaims(token); // JwtUtil을 사용하도록 변경
        if (claims == null) {
            log.warn("[JwtUtil] JWT Claims가 null입니다.");
            return null;
        }
        try {
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else {
                log.warn("[JwtUtil] userId의 데이터 타입이 예상과 다릅니다. 값: {}", userIdObj);
                return null;
            }
        } catch (Exception e) {
            log.error("[JwtUtil] userId 추출 중 오류 발생", e);
            return null;
        }
    }
    public String getEmail() {
        return user.getEmail();
    }
    public String getRole() {
        return user.getRole().name();
    }
    public UserEntity getUserEntity() {
        return user;
    }
}