package com.shinhan.peoch.card.controller;
import com.shinhan.entity.CardDesignEntity;
import com.shinhan.entity.CardEntity;
import com.shinhan.entity.InvestmentEntity;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.card.dto.CardRequestDTO;
import com.shinhan.peoch.card.dto.UserInfoDTO;
import com.shinhan.peoch.card.service.CardApplicationService;
import com.shinhan.peoch.card.service.CardApplicationService;
import com.shinhan.peoch.design.dto.CardDesignDTO;
import com.shinhan.peoch.design.service.CardDesignService;
import com.shinhan.peoch.security.SecurityUser;
import com.shinhan.peoch.security.jwt.JwtTokenProvider;
import com.shinhan.repository.CardRepository;
import com.shinhan.repository.InvestmentRepository;
import com.shinhan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
public class CardApplicationController {
    private final JwtTokenProvider jwtTokenProvider;
    private final CardApplicationService cardService;
    private final CardDesignService cardDesignService;

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final InvestmentRepository investmentRepository;



    @PostMapping("/insert")
    public ResponseEntity<?> applyCard(
            @RequestPart("cardDesignDTO") CardDesignDTO cardDesignDTO,
            @RequestPart("cardRequestDTO") CardRequestDTO cardRequestDTO,

            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @CookieValue(value = "jwt", required = false) String jwtToken
    ) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("🚨 [ContractController] JWT 쿠키 없음!");
            return ResponseEntity.status(401).body(Map.of("error", "인증 토큰이 없습니다."));
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        // 로깅으로 전달된 값 확인
        log.info("CardRequestDTO: {}", cardDesignDTO);

        if (imageFile != null) {
            log.info("이미지 파일 이름: {}", imageFile.getOriginalFilename());
        } else {
            log.info("이미지 파일 없음");
        }


        cardService.createCardApplication(userId, cardRequestDTO, cardDesignDTO, imageFile);

        return ResponseEntity.ok("카드 신청이 완료되었습니다.");


    }

    @GetMapping("/userInfo")
    public ResponseEntity<?> getUserInfo(@CookieValue(value = "jwt", required = false) String jwtToken) {
        // 현재 JWT 적용 전이므로 userId를 고정 (예: 16L)
        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("🚨 [ContractController] JWT 쿠키 없음!");
            return ResponseEntity.status(401).body(Map.of("error", "인증 토큰이 없습니다."));
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        Optional<InvestmentEntity> investmentOpt = investmentRepository.findByUserId(userId.intValue());
        System.out.println("InvestmentOp" + investmentOpt);



        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        UserInfoDTO dto = new UserInfoDTO();
        dto.setAddress(user.getAddress());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        if(investmentOpt.isPresent()){
            InvestmentEntity inv = investmentOpt.get();
            dto.setMonthlyAllowance(inv.getMonthlyAllowance());
            dto.setMaxInvestment(inv.getMaxInvestment());
            dto.setEndDate(inv.getEndDate());
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/cardInfo")
    public ResponseEntity<?> getUserCardInfo(@CookieValue(value = "jwt", required = false) String jwtToken) {
        // 현재 JWT 적용 전이므로 userId를 고정 (예: 16L)
        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("🚨 [ContractController] JWT 쿠키 없음!");
            return ResponseEntity.status(401).body(Map.of("error", "인증 토큰이 없습니다."));
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);


        // InvestmentEntity 조회 후 Optional 체크 (투자 정보가 없는 경우 처리 예시)
        Optional<InvestmentEntity> investmentOpt = investmentRepository.findByUserId(userId.intValue());
        System.out.println("InvestmentOp: " + investmentOpt);
        if (!investmentOpt.isPresent()) {
            // 투자 정보가 없으면, 필요에 따라 다른 응답을 반환하거나 예외를 처리할 수 있음
            return ResponseEntity.ok(
                    Map.of("cardRegistered", Map.of("invest", false))
            );

        }

        log.info("--------!!!!!!!!!!!!!!!----------------------------------------");
        log.info(userId.toString());

// 카드 정보 조회
        CardEntity cardRegistered = cardRepository.findByUser_UserId(userId);

// 카드 정보가 없으면 예외 처리 혹은 대체 응답 처리
        if (cardRegistered == null) {
            log.info("카드 정보가 존재하지 않습니다.");
            return ResponseEntity.ok( Map.of("cardRegistered", Map.of("cardRegistered", false)));
        }

        log.info("--------!!!!!!!!!!!!!!!----------------------------------------");
        log.info(cardRegistered.toString());

// 카드 정보가 있으면 해당 정보를 반환
        return ResponseEntity.ok( Map.of("cardRegistered", Map.of("cardRegistered", true)));
    }
}
