package com.shinhan.peoch.security;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.shinhan.peoch.auth.entity.UserEntity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class SecurityUser extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String ROLE_PREFIX = "ROLE_";

    private final Long userId;
    private final String name;
    private final String email;
    private final LocalDate birthdate;
    private final String role;

    public SecurityUser(UserEntity user) {
        super(user.getEmail(), user.getPassword(), makeRole(user));
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.birthdate = user.getBirthdate();
        this.role = user.getRole().name();
    }
    

    private static Collection<? extends GrantedAuthority> makeRole(UserEntity user) {
        Collection<GrantedAuthority> roleList = new ArrayList<>();
        roleList.add(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name()));
        return roleList;
    }

    @Override
    public String toString() {
        return "SecurityUser(userId=" + userId + ", email=" + email + ", name=" + name + ", role=" + role + ")";
    }
}
