package com.shinhan.peoch.invest.controller;

import com.shinhan.peoch.invest.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class OcrController {
    private final OcrService ocrService;

    @PostMapping("/api/ocr")
    public ResponseEntity<String> extractText(@RequestParam("file") MultipartFile file) {
        try {
            //업로드된 파일을 임시 디렉토리에 저장
            File tempFile = File.createTempFile("ocr_", ".png");
            file.transferTo(tempFile);

            //OCR 실행
            String extractedText = ocrService.extractTextFromImage(tempFile.getAbsolutePath());

            tempFile.delete();

            return ResponseEntity.ok(extractedText);
        } catch(IOException e) {
            return ResponseEntity.status(500).body("파일 처리 중 오류가 발생했습니다." + e.getMessage());
        }
    }
}
