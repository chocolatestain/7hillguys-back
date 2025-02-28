package com.shinhan.peoch.security.jwt;

import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String login(UserEntity dto) {
        String email = dto.getEmail();
        String password = dto.getPassword();
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            throw new UsernameNotFoundException("해당 이메일의 사용자가 존재하지 않습니다.");
        }

        //암호화된 password를 디코딩한 값과 입력한 패스워드 값이 다르면 null 반환
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user);
        return accessToken;
    }
}
