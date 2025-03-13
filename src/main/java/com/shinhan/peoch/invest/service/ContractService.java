package com.shinhan.peoch.invest.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Text;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
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

import java.io.ByteArrayOutputStream;
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
            InvestmentEntity investment = investmentRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("해당 투자 정보를 찾을 수 없습니다."));

            UserEntity user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

            UserProfileEntity userProfile = userProfileRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                    .orElseThrow(() -> new RuntimeException("사용자 프로필 정보를 찾을 수 없습니다."));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // 한글 폰트 설정
            String fontPath = "src/main/resources/font/NotoSansKR-Regular.ttf";
            PdfFont font;
            try {
                font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H);
            } catch (Exception e) {
                log.error("🚨 폰트 파일 로드 실패! 경로 확인 필요: {}", fontPath, e);
                throw new RuntimeException("🚨 폰트 파일 로드 실패", e);
            }
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            try {
                // 계약 제목
                document.add(new Paragraph(new Text("대출 계약서").setFont(font).setBold().setFontSize(18)));

                // 계약 당사자 정보
                document.add(new Paragraph(new Text("1. 계약 당사자 정보").setFont(font).setBold()));
                document.add(new Paragraph(new Text(" - 투자자 (대출 기관): 피치 투자 금융 서비스").setFont(font)));
                document.add(new Paragraph(new Text(" - 대출자 (고객명): " + user.getName()).setFont(font)));
                // 성별 변환 (0: 남성, 1: 여성)
                String genderStr = (userProfile.getGender() != null && userProfile.getGender()) ? "여성" : "남성";
                document.add(new Paragraph(new Text(" - 대출자 성별: " + genderStr).setFont(font)));
                document.add(new Paragraph(new Text(" - 대출자 생년월일: " + user.getBirthdate()).setFont(font)));
                document.add(new Paragraph(new Text(" - 대출자 연락처: " + user.getPhone()).setFont(font)));
                document.add(new Paragraph(new Text(" - 대출자 주소: " + userProfile.getAddress()).setFont(font)));

                // 투자 조건
                document.add(new Paragraph(new Text("2. 대출 조건").setFont(font).setBold()));
                document.add(new Paragraph(new Text(" - 투자 금액: " + investment.getOriginalInvestValue() + " 원").setFont(font)));
                document.add(new Paragraph(new Text(" - 최대 투자 가능 금액: " + investment.getMaxInvestment() + " 원").setFont(font)));
                document.add(new Paragraph(new Text(" - 투자 시작일: " + investment.getStartDate()).setFont(font)));
                document.add(new Paragraph(new Text(" - 투자 종료일: " + investment.getEndDate()).setFont(font)));
                document.add(new Paragraph(new Text(" - 월 지급액: " + investment.getMonthlyAllowance() + " 원").setFont(font)));

                //상환 조건
                document.add(new Paragraph(new Text("3. 상환 조건").setFont(font).setBold()));
                document.add(new Paragraph(new Text(" - 상환 개시일: " + investment.getEndDate()).setFont(font)));

                //조기 상환 규정
                document.add(new Paragraph(new Text("4. 조기 상환 규정").setFont(font).setBold()));

                // 법적 책임 및 기타 약관
                document.add(new Paragraph(new Text("6. 법적 책임 및 기타 약관").setFont(font).setBold()));
                document.add(new Paragraph(new Text(" - 본 계약서는 상호 동의 하에 체결됩니다.").setFont(font)));
                document.add(new Paragraph(new Text(" - 상환 일정은 변동될 수 있으며, 연체 시 이자가 부과될 수 있습니다.").setFont(font)));
                document.add(new Paragraph(new Text(" - 본 계약과 관련된 모든 분쟁은 대한민국 법률에 따라 해결됩니다.").setFont(font)));
                document.add(new Paragraph(new Text(" - 기타 사항은 대출 기관의 약관을 따릅니다.").setFont(font)));

                // 전자 서명 추가
                if (base64Signature != null && !base64Signature.isEmpty()) {
                    try {
                        byte[] imageBytes = Base64.getDecoder().decode(base64Signature.split(",")[1]);
                        ImageData imageData = ImageDataFactory.create(imageBytes);
                        Image signatureImage = new Image(imageData);
                        signatureImage.scaleToFit(120, 40); //서명크기
//                        document.add(new Paragraph("서명:"));
//                        document.add(signatureImage);
                        // 서명 텍스트와 서명을 한 줄에 배치
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

            // 계약서 PDF 및 서명을 DB에 저장
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
/*
document.add(new Paragraph(new Text(" - 월 상환 퍼센트: " + investment.getStartDate()).setFont(font)));
            document.add(new Paragraph(new Text(" - 연체 시 연체 이자율: " + investment.getStartDate()).setFont(font)));
            document.add(new Paragraph(new Text(" - 연체 시 추가 페널티: " + investment.getStartDate()).setFont(font)));

            document.add(new Paragraph(new Text(" - 최소 상황 금액: " + investment.getOriginalInvestValue() + " 원").setFont(font)));
            document.add(new Paragraph(new Text(" - 최대 상황 금액: " + investment.getOriginalInvestValue() + " 원").setFont(font)));
document.add(new Paragraph(new Text(" - 조기 상환 수수료: " + investment.getEarlyRepaymentFee() + " 원").setFont(font)));*/
