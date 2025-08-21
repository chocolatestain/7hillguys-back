package com.shinhan.peoch.invest.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.shinhan.entity.InvestmentEntity;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.repository.InvestmentRepository;
import com.shinhan.repository.UserProfileRepository;
import com.shinhan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {
    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    // 계약서 PDF 생성 및 저장
   @Transactional
public byte[] generateAndSaveContractPdf(Integer userId, String base64Signature) throws Exception {
    try {
        InvestmentEntity investment = investmentRepository.findInvestmentByUserId(userId.longValue());
        if (investment == null) throw new RuntimeException("투자 정보를 찾을 수 없습니다.");

        UserEntity user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        UserProfileEntity userProfile = userProfileRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("사용자 프로필 정보를 찾을 수 없습니다."));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // ✅ 한글 폰트: 클래스패스에서 로드
        PdfFont font;
        try {
            Resource fontRes = new ClassPathResource("font/NotoSansKR-Regular.ttf");
            byte[] fontBytes;
            try (var is = fontRes.getInputStream()) {
                fontBytes = is.readAllBytes();
            }
            font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);

        } catch (Exception e) {
            log.error("🚨 폰트 파일 로드 실패! classpath:font/NotoSansKR-Regular.ttf", e);
            throw new RuntimeException("🚨 폰트 파일 로드 실패", e);
        }

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        try {
            // 계약 제목
            document.add(new Paragraph(new Text("대출 계약서")
                    .setFont(font).setBold().setFontSize(18))
                    .setTextAlignment(TextAlignment.CENTER));

            NumberFormat formatter = NumberFormat.getInstance();

            // 계약 당사자 정보
            document.add(new Paragraph(new Text("1. 계약 당사자 정보").setFont(font).setBold()));
            document.add(new Paragraph(new Text(" - 투자자 (대출 기관): 피치 투자 금융 서비스").setFont(font)));
            document.add(new Paragraph(new Text(" - 대출자 (고객명): " + user.getName()).setFont(font)));
            String genderStr = (userProfile.getGender() != null && userProfile.getGender()) ? "여성" : "남성";
            document.add(new Paragraph(new Text(" - 성별: " + genderStr + "/ 생년월일: " + user.getBirthdate()).setFont(font)));
            document.add(new Paragraph(new Text(" - 대출자 연락처: " + user.getPhone()).setFont(font)));
            document.add(new Paragraph(new Text(" - 주소: " + userProfile.getAddress()).setFont(font)));

            // 투자 조건
            document.add(new Paragraph(new Text("2. 대출 조건").setFont(font).setBold()));
            document.add(new Paragraph(new Text(" - 투자 금액: " + formatter.format(investment.getOriginalInvestValue()) + " 원").setFont(font)));
            document.add(new Paragraph(new Text(" - 투자 시작일: " + investment.getStartDate()).setFont(font)));
            document.add(new Paragraph(new Text(" - 투자 종료일: " + investment.getEndDate()).setFont(font)));
            document.add(new Paragraph(new Text(" - 월 지급액: " + formatter.format(investment.getMonthlyAllowance()) + " 원").setFont(font)));

            // 상환 조건
            var birthDate = user.getBirthdate();
            var retirementDate = birthDate.plusYears(55);
            var retirementDateFormatted = retirementDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            document.add(new Paragraph(new Text("3. 상환 조건").setFont(font).setBold()));
            document.add(new Paragraph(new Text(" - 상환 개시일: " + investment.getEndDate() + " ~ " + retirementDateFormatted).setFont(font)));
            document.add(new Paragraph(new Text(" - 상환 금액: 월급여의 " + investment.getRefundRate() + "%").setFont(font)));

            // 약관
            document.add(new Paragraph(new Text("4. 법적 책임 및 기타 약관").setFont(font).setBold()));
            document.add(new Paragraph(new Text(" - 본 계약서는 상호 동의 하에 체결됩니다.").setFont(font)));
            document.add(new Paragraph(new Text(" - 이용자가 원하면 계약은 중도에 해지할 수 있습니다.").setFont(font)));
            document.add(new Paragraph(new Text(" - 본 계약과 관련된 모든 분쟁은 대한민국 법률에 따라 해결됩니다.").setFont(font)));
            document.add(new Paragraph(new Text(" - 기타 사항은 대출 기관의 약관을 따릅니다.").setFont(font)));

            // 전자 서명 (data URL/순수 Base64 모두 대응)
            if (base64Signature != null && !base64Signature.isEmpty()) {
                try {
                    String b64 = base64Signature.contains(",")
                            ? base64Signature.substring(base64Signature.indexOf(',') + 1)
                            : base64Signature;
                    byte[] imageBytes = Base64.getDecoder().decode(b64);
                    ImageData imageData = ImageDataFactory.create(imageBytes);
                    Image signatureImage = new Image(imageData).scaleToFit(80, 20);

                    Paragraph signatureParagraph = new Paragraph()
                            .setTextAlignment(TextAlignment.LEFT)
                            .add(new Text("\n서명: ").setFont(font).setBold())
                            .add(signatureImage);

                    document.add(signatureParagraph);
                } catch (Exception e) {
                    log.error("🚨 서명 이미지 처리 실패!", e);
                    throw new RuntimeException("🚨 서명 이미지 처리 중 오류 발생", e);
                }
            }
        } finally {
            document.close();
            pdfDocument.close();
        }

        byte[] pdfBytes = outputStream.toByteArray();

        // DB 저장
        try {
            investment.setContractPdf(pdfBytes);
            investment.setSignature(base64Signature);
            investmentRepository.save(investment);
            log.info("✅ 계약서 PDF 저장 완료 (크기: {} bytes)", pdfBytes.length);
        } catch (Exception e) {
            log.error("🚨 DB 저장 실패! userId: {}", userId, e);
            throw new RuntimeException("🚨 DB 저장 중 오류 발생", e);
        }

        return pdfBytes;
    } catch (Exception e) {
        log.error("🚨 계약서 생성 중 오류 발생! userId: {}", userId, e);
        throw e;
    }
}
}
