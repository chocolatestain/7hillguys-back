package com.shinhan.peoch.auth.service;

import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.auth.repository.UserRepository;
import com.shinhan.peoch.security.SecurityUser;
import com.shinhan.peoch.security.jwt.JwtUtil;
import com.shinhan.peoch.security.jwt.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    //회원가입
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

    //로그인 (Access Token + Refresh Token 발급)
    public Map<String, String> login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(email, user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // Redis에 Refresh Token 저장
        tokenService.saveRefreshToken(email, refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    //Refresh Token을 이용한 Access Token 재발급
    public String refreshAccessToken(String email, String refreshToken) {
        String storedToken = tokenService.getRefreshToken(email);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return jwtUtil.generateAccessToken(email, user.getRole().name());
    }

    //로그아웃 (Refresh Token 삭제)
    public String logout(String email) {
        tokenService.deleteRefreshToken(email);
        return "로그아웃 성공!";
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return new SecurityUser(user);
    }
}
