package com.shinhan.peoch.invest.controller;

import com.shinhan.entity.InvestmentEntity;
import com.shinhan.peoch.invest.dto.SignDTO;
import com.shinhan.peoch.lifecycleincome.service.ExpectedValueService;
import com.shinhan.peoch.security.jwt.JwtTokenProvider;
import com.shinhan.repository.InvestmentRepository;
import com.shinhan.peoch.invest.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
public class ContractController {
    private final InvestmentRepository investmentRepository;
    private final ContractService contractService;
    private final ExpectedValueService expectedValueService;
    private final JwtTokenProvider jwtTokenProvider;

    // 1 계약서 기본 형식 반환 (프론트엔드에서 계약서 미리보기)
    @GetMapping("/template")
    public ResponseEntity<Map<String, Object>> getContractTemplate(
            @CookieValue(value = "jwt", required = false) String jwtToken) {

        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("🚨 [ContractController] JWT 쿠키 없음!");
            return ResponseEntity.status(401).body(Map.of("error", "인증 토큰이 없습니다."));
        }

        // JWT에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userId == null) {
            log.warn("🚨 [ContractController] JWT에서 userId 추출 실패!");
            return ResponseEntity.status(401).body(Map.of("error", "잘못된 JWT입니다."));
        }

        InvestmentEntity investmentOpt = investmentRepository.findInvestmentByUserId(userId);
        if (investmentOpt==null) {
            return ResponseEntity.status(404).body(Map.of("error", "해당 사용자의 계약서 데이터를 찾을 수 없습니다."));
        }

        InvestmentEntity investment = investmentOpt;
        Double maxRepaymentAmount = expectedValueService.calculateTotalExpectedIncome(userId.intValue());

        // 계약서 기본 내용 (사용자 데이터 포함)
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);

        String monthlyAllowanceStr = nf.format(investment.getMonthlyAllowance());
        String maxInvestmentStr = nf.format(investment.getMaxInvestment());
        String originalInvestValueStr = nf.format(investment.getOriginalInvestValue());

        Map<String, Object> contractData = new HashMap<>();
        contractData.put("title", "계약 사항");
        contractData.put("investmentDate", String.format(
                "%s - %s 매월 %s 원을 지급받습니다.", investment.getStartDate(), investment.getEndDate(), monthlyAllowanceStr));
        contractData.put("investmentMoney",String.format(
                "총 %s 원을 지원받습니다. ", originalInvestValueStr));
        contractData.put("investmentTotal",String.format(
                "최대 투자 금액은 %s 원이며, 투자금액 변동에 따라 상환 비율이 조정됩니다.", maxInvestmentStr));

        contractData.put("repaymentTerms", String.format(
                "%s - 55세가 되는 년도까지 입니다.",
                investment.getEndDate()));
        contractData.put("repaymentTerms2", String.format(
                "월 상환 금액은 %.3f%%입니다.",
                investment.getRefundRate()));

        contractData.put("agreements", new String[]{
                "본 계약서는 상호 동의 하에 체결됩니다.",
                "이용자는 중도에 계약을 해지할 수 있습니다. 단, 최대 상환 금액이 부과될 수 있습니다.",
                "상환 일정은 변동될 수 있으며, 연체 시 이자가 부과될 수 있습니다."
        });

        return ResponseEntity.ok(contractData);
    }

    // 2.계약서 서명 후 PDF 생성 및 저장
    @PostMapping("/sign")
    public ResponseEntity<byte[]> signContract(
            @CookieValue(value = "jwt", required = false) String jwtToken,
            @RequestBody SignDTO request) {

        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("🚨 [ContractController] JWT 쿠키 없음! 서명 요청 차단.");
            return ResponseEntity.status(401).body(null);
        }

        // JWT에서 userId 추출
        Long userIdLong = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userIdLong == null) {
            log.warn("🚨 [ContractController] JWT에서 userId 추출 실패!");
            return ResponseEntity.status(401).body(null);
        }

        // Long → Integer 변환
        Integer userId = userIdLong.intValue();

        try {

            byte[] pdf = contractService.generateAndSaveContractPdf(userId, request.getBase64Signature());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            //headers.setContentDispositionFormData("filename", "signed_contract.pdf");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=signed_contract.pdf");

            log.info("✅ 계약서 PDF 생성 성공! userId={}, 크기: {} bytes", userId, pdf.length);

            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            log.error("🚨 계약서 서명 중 오류 발생! userId: {}", userId, e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
}