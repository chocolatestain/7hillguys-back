package com.shinhan.peoch.auth.service;

import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.auth.repository.UserRepository;
import com.shinhan.peoch.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public String register(UserEntity user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 사용자입니다.");
        }

        UserEntity newUser = UserEntity.builder()
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))  // 비밀번호 암호화
                .name(user.getName())
                .birthdate(user.getBirthdate())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .build();

        userRepository.save(newUser);
        return "회원가입 성공!";
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("---email: " + email + " ---");

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return new SecurityUser(user);
    }
}
