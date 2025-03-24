package com.shinhan.peoch.invest.controller;

import com.shinhan.entity.UserProfileEntity;
import com.shinhan.peoch.UserProfileNormalization.perplexity.UserProfileNormalizationPerplexityService;
import com.shinhan.peoch.UserProfileNormalization.service.AsyncProcessingService;
import com.shinhan.peoch.invest.dto.UserProfileDTO;
import com.shinhan.peoch.invest.service.UserProfileFileService;
import com.shinhan.peoch.invest.service.UserProfileService;
import com.shinhan.peoch.lifecycleincome.service.InvestmentService;
import com.shinhan.peoch.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final UserProfileFileService userProfileFileService;
    private final JwtTokenProvider jwtTokenProvider;
    private final InvestmentService investmentService;
    private final UserProfileNormalizationPerplexityService userProfileNormalizationPerplexityService;
    private final AsyncProcessingService asyncProcessingService;




    /**
     * 1.UserProfileDTO 들어오는대로 저장
     * 2. 비동기로 normprofile 만들고 저장
     * @param dto
     * @param jwt
     * @return
     */
    @PostMapping("/save")
    public ResponseEntity<UserProfileEntity> saveUserProfile(@RequestBody UserProfileDTO dto, @CookieValue(value = "jwt", required = false) String jwt) {
        if (jwt == null || jwt.isEmpty()) {
            log.warn("[saveUserProfile] JWT 쿠키가 존재하지 않음!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        log.info("[saveUserProfile] 요청 도착, JWT: {}", jwt);

        // JWT 검증 및 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
        if (userId == null) {
            log.warn("[saveUserProfile] JWT에서 userId 추출 실패!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // userId를 DTO에 설정
        dto.setUserId(userId.intValue());


        //profile 만들기
        UserProfileEntity savedProfile = userProfileService.saveUserProfile(dto);

        // 비동기 작업 트리거
        asyncProcessingService.profileToExpectedIncome(savedProfile.getUserProfileId(), Math.toIntExact(userId));


        return ResponseEntity.ok(savedProfile);

    }


    @PostMapping("/file")
    public ResponseEntity<String> submitUserProfile(
            @RequestParam(value = "universityCertificate", required = false) MultipartFile universityCertificate,
            @RequestParam(value = "studentCardFile", required = false) MultipartFile studentCardFile,
            @RequestParam(value = "certificationFiles", required = false) MultipartFile[] certificationFiles,
            @RequestParam(value = "familyCertificate", required = false) MultipartFile familyCertificate,
            @RequestParam(value = "criminalRecordFile", required = false) MultipartFile criminalRecordFile
    ) {
        try {
            log.info("[submitUserProfile] 파일 업로드 요청 수신");

            // 업로드된 파일 목록 확인
            log.info("대학 증명서 파일: {}", (universityCertificate != null ? universityCertificate.getOriginalFilename() : "없음"));
            log.info("학생증 파일: {}", (studentCardFile != null ? studentCardFile.getOriginalFilename() : "없음"));
            log.info("가족 증명서 파일: {}", (familyCertificate != null ? familyCertificate.getOriginalFilename() : "없음"));
            log.info("범죄 기록 파일: {}", (criminalRecordFile != null ? criminalRecordFile.getOriginalFilename() : "없음"));

            // 자격증 파일 배열 로그 추가
            if (certificationFiles != null && certificationFiles.length > 0) {
                for (MultipartFile certFile : certificationFiles) {
                    System.out.println("📂 자격증 파일: " + certFile.getOriginalFilename());
                }
            } else {
                System.out.println("❌ 자격증 파일 없음");
            }


            // 파일 저장 경로 설정
            String universityFilePath = (universityCertificate != null && !universityCertificate.isEmpty()) ?
                    userProfileFileService.saveFile(universityCertificate, "university") : "파일 없음";
            String studentCardFilePath = (studentCardFile != null && !studentCardFile.isEmpty()) ?
                    userProfileFileService.saveFile(studentCardFile, "student_card") : "파일 없음";
            String familyFilePath = (familyCertificate != null && !familyCertificate.isEmpty()) ?
                    userProfileFileService.saveFile(familyCertificate, "family") : "파일 없음";
            String criminalFilePath = (criminalRecordFile != null && !criminalRecordFile.isEmpty()) ?
                    userProfileFileService.saveFile(criminalRecordFile, "criminal") : "파일 없음";

            // 자격증 파일 저장
            StringBuilder certPaths = new StringBuilder();
            if (certificationFiles != null && certificationFiles.length > 0) {
                log.info("자격증 파일 개수: {}", certificationFiles.length);
                for (MultipartFile certFile : certificationFiles) {
                    if (certFile != null && !certFile.isEmpty()) {
                        String certPath = userProfileFileService.saveFile(certFile, "certificate");
                        certPaths.append(certPath).append("\n");
                        log.info("[submitUserProfile] 자격증 파일 저장 완료: {}", certPath);
                    }
                }
            } else {
                log.info("자격증 파일 없음.");
            }

            return ResponseEntity.ok("파일 업로드 성공\n"
                    + "대학 증명서: " + universityFilePath + "\n"
                    + "학생증: " + studentCardFilePath + "\n"
                    + "가족 증명서: " + familyFilePath + "\n"
                    + "범죄 기록: " + criminalFilePath + "\n"
                    + "자격증:\n" + certPaths.toString());
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("파일 업로드 실패: " + e.getMessage());
        }
    }
}