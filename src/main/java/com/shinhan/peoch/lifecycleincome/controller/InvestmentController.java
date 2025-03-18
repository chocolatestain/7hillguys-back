package com.shinhan.peoch.lifecycleincome.controller;

import com.shinhan.entity.InvestmentEntity;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.peoch.UserProfileNormalization.perplexity.UserProfileNormalizationPerplexityService;
import com.shinhan.peoch.invest.service.UserProfileService;
import com.shinhan.peoch.lifecycleincome.DTO.*;
import com.shinhan.peoch.lifecycleincome.service.ExitCostService;
import com.shinhan.peoch.lifecycleincome.service.InvestmentService;
import com.shinhan.peoch.lifecycleincome.service.SetInvestAmountService;
import com.shinhan.peoch.security.jwt.JwtTokenProvider;
import com.shinhan.peoch.security.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class InvestmentController {

    @Autowired
    JwtUtil jwtUtil;

    @Value("${jwt.secret}")  // application.yml의 secret 가져오기
    String secretKey;

    @Autowired
    InvestmentService investmentService;

    @Autowired
    SetInvestAmountService setInvestAmountService;

    @Autowired
    ExitCostService exitCostService;

    @Autowired
    UserProfileService userProfileService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserProfileNormalizationPerplexityService userProfileNormalizationPerplexityService;

    /**
     * investment 생성
     * 해당 userid에 초기 investment 만듬
     * @param jwtToken
     * @return
     */
    @PostMapping("/investment")
    public InvestmentEntity saveInvestment(
            @CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return null;
        }
        Integer userId = userIdLong.intValue();

        return investmentService.createInvestment(userId);
    }


    /**
     * 환급률 업데이트 및 리턴
     * @param jwtToken
     * @return
     */
    @GetMapping("/investment/refund-rate")
    public ResponseEntity<Double> updateRefundRate(
            @CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return ResponseEntity.status(401).body(null);
        }
        Integer userId = userIdLong.intValue();

        double refundRate = investmentService.updateRefundRate(userId);
        return ResponseEntity.ok(refundRate);
    }


    /**
     * 임시 한도 페이지를 위한 상세 정보 조회
     * @param jwtToken
     * @return
     */
    @GetMapping("/investment/tempallowance")
    public InvestmentTempAllowanceDTO getInvestmentDetails(
            @CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            System.out.println("토큰없어");
            return null;
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            System.out.println("id가 없엉");
            return null;
        }
        Integer userId = userIdLong.intValue();
        System.out.println(investmentService.calculateInvestmentDetails(userId).toString());
        return investmentService.calculateInvestmentDetails(userId);
    }


    /**
     * 퇴직 응답 정보 조회
     * @param jwtToken
     * @return
     */
    @GetMapping("/investment/exit")
    public ResponseEntity<ExitResponseDTO> exitResponse(
            @CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return ResponseEntity.status(401).body(null);
        }
        Integer userId = userIdLong.intValue();

        ExitResponseDTO exitResponseDTO;
        try {
            exitResponseDTO = exitCostService.exitResponseService(userId);
        } catch (Exception e) {
            exitResponseDTO = ExitResponseDTO.builder().message("현재 예상 소득 산출중입니다. 3분 이내로 완료 됩니다.").build();
        }
        return ResponseEntity.ok(exitResponseDTO);
    }

    /**
     * 실제 퇴직 정보 조회
     * @param jwtToken
     * @return
     */
    @GetMapping("/investment/reallyexit")
    public ResponseEntity<ReallyExitResponseDTO> getInvestmentExitInfo(
            @CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return ResponseEntity.status(401).body(null);
        }
        Integer userId = userIdLong.intValue();

        ReallyExitResponseDTO response = investmentService.getInvestmentExitInfo(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 투자 금액 설정 데이터 조회
     * @param jwtToken
     * @return
     */
    @GetMapping("/investment/setamount")
    public ResponseEntity<SetInvestAmountDTO> getInvestmentData(
            @CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return ResponseEntity.status(401).body(null);
        }
        Integer userId = userIdLong.intValue();

        //UserID에 해당되는 profile 중에 가장 최신 profile챙겨옴
        UserProfileEntity userProfileEntity = userProfileService.findUserProfileByUserIdOrderByUpdatedAtDesc(Long.valueOf(userId));
        SetInvestAmountDTO response = setInvestAmountService.getInvestmentData(userProfileEntity.getUserProfileId());
        return ResponseEntity.ok(response);
    }

    /**
     * 예상 환급률 계산
     * @param jwtToken
     * @param requestDTO
     * @return
     */
    @PostMapping("/investment/refund-rate")
    public ResponseEntity<Double> expectedRefundRate(
            @CookieValue(value = "jwt", required = false) String jwtToken,
            @RequestBody InvestmentRequestDTO requestDTO) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return ResponseEntity.status(401).body(null);
        }
        Integer userId = userIdLong.intValue();

        requestDTO.setUserId(userId);
        double refundRate = investmentService.checkRefundRate(requestDTO.getUserId(), requestDTO.getInvestAmount());
        return ResponseEntity.ok(refundRate);
    }

    /**
     * 임시 수당 적용
     * @param jwtToken
     * @param requestBody
     * @return
     * @throws IOException
     */
    @PostMapping("/investment/applytempallowance")
    public ResponseEntity<?> Postapplytempallowance(
            @CookieValue(value = "jwt", required = false) String jwtToken,
            @RequestBody Map<String, Integer> requestBody) throws IOException {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return ResponseEntity.status(401).body(null);
        }
        Integer userId = userIdLong.intValue();

        //만원 단위로 사용
        Integer amount = (requestBody.get("amount")/10000)* 10000;

        ApiResponseDTO<String> result = investmentService.setTempAllowance(amount, Long.valueOf(userId));

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 실제 투자 종료 처리
     * @param jwtToken
     * @return
     * @throws IOException
     */
    @PostMapping("/investment/reallyexit")
    public ResponseEntity<?> PostInvestmentReallyExit(
            @CookieValue(value = "jwt", required = false) String jwtToken) throws IOException {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return ResponseEntity.status(401).body(null);
        }
        Integer userId = userIdLong.intValue();

        //로직 처리
        //기존 엑시트 비용함수랑 누적 환급 금액이 일치하거나 더 많으면 해지 됨
        ApiResponseDTO<String> result = setInvestAmountService.stopInvestment(Long.valueOf(userId));
        System.out.println(result);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * SetAmountRequestDTO기반으로 investment 설정
     * @param jwtToken
     * @param requestDTO
     * @return
     */
    @PostMapping("/investment/setamount")
    public ResponseEntity<ApiResponseDTO<String>> postSetAmount(
            @CookieValue(value = "jwt", required = false) String jwtToken,
            @RequestBody SetAmountRequestDTO requestDTO) {
        // JWT 토큰 검증
        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.error("JWT 토큰이 없습니다.", "UNAUTHORIZED"));
        }


        // JWT에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.error("유효하지 않은 JWT 토큰입니다.", "INVALID_TOKEN"));
        }
        System.out.println(requestDTO.toString());

        // 투자 설정
        ApiResponseDTO<String> response = setInvestAmountService.setInvestment(userId, requestDTO);
        System.out.println(response);
        // 결과 리턴
        if (response.isSuccess()) {
            return ResponseEntity.ok(response); // 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 BAD REQUEST
        }
    }
    @GetMapping("/investment/test")
    public ResponseEntity<?> getInvestmadfils(
            @CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }
        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            return null;
        }
        Integer userId = userIdLong.intValue();

        try {
            ResponseEntity<ApiResponseDTO<String>> result = userProfileNormalizationPerplexityService
                    .normalizeAndSaveUserProfile(userId);
            return ResponseEntity.ok(result);
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("프로필 정규화 중 충돌이 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }


}
