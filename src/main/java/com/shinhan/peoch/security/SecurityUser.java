package com.shinhan.peoch.security;

import com.shinhan.peoch.auth.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;

public class SecurityUser extends User {
    private static final String ROLE_PREFIX = "ROLE_";

    public SecurityUser(UserEntity user) {
        super(user.getEmail(), user.getPassword(), makeRole(user));
    }

    private static Collection<? extends GrantedAuthority> makeRole(UserEntity user) {
        Collection<GrantedAuthority> roleList = new ArrayList<>();
        roleList.add(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole()));
        return roleList;
    }

    public SecurityUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }
}
