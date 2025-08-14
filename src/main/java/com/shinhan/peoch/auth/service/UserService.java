package com.shinhan.peoch.auth.service;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.shinhan.peoch.auth.dto.UserResponseDTO;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.security.SecurityUser;
import com.shinhan.peoch.security.jwt.JwtUtil;
import com.shinhan.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
    public UserDetails loadUserByUsername(String email) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("이메일 없음: " + email));
    
        return new SecurityUser(user);  
    }
    

    public UserResponseDTO getCurrentUser(@AuthenticationPrincipal UserEntity userEntity) {
        if (userEntity == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        return new UserResponseDTO(userEntity.getUserId());
    }
    public UserEntity getUserById(Long userId) {
        // Optional을 사용하여 userId로 사용자 검색
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다."));
    }

}
