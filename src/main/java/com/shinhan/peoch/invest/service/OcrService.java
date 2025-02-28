package com.shinhan.peoch.invest.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OcrService {

    public String extractTextFromImage(String imagePath) {
        ITesseract tesseract = new Tesseract();

        //Tesseract 실행 파일 경로 (Windows 사용자만 설정 필요)
        //tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR");

        //언어 설정 (한국어: "kor", 영어: "eng", 둘다: "kor+eng")
        tesseract.setLanguage("kor");

        try {
            return tesseract.doOCR(new File(imagePath));
        } catch(TesseractException e) {
            return "OCR 오류 발생" + e.getMessage();
        }
    }
}
