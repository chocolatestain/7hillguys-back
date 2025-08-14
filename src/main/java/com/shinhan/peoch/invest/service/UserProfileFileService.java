package com.shinhan.peoch.invest.service;

import com.shinhan.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileFileService {
    private final UserProfileRepository userProfileRepository;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/uploads/";
    //private final AmazonS3 amazonS3;

    /** 로컬 서버에 파일 저장 **/
    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            //원래 파일명에서 확장자 추출
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            //파일명에서 prefix 자동 설정
            String prefix = "file";
            if (originalFileName.contains("university")) {
                prefix = "university";
            } else if (originalFileName.contains("certificate")) {
                prefix = "certificate";
            } else if (originalFileName.contains("student")) {
                prefix = "student";
            } else if(originalFileName.contains("family")) {
                prefix = "family";
            } else if(originalFileName.contains("criminal")) {
                prefix = "criminal";
            }

            //UUID + prefix + 확장자로 파일명 저장
            String fileName = prefix + "_" + UUID.randomUUID() + extension;
            String filePath = Paths.get(UPLOAD_DIR, fileName).toString();

            // 폴더가 없으면 생성
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            //파일 저장
            File destFile = new File(filePath);
            file.transferTo(destFile);

            return "/static/images/" + fileName;  // DB에 저장할 파일명 반환
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    //파일 업로드 처리 (여러 파일 처리)
    public String saveFile(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            // 원래 파일명에서 확장자 추출
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // UUID + prefix + 확장자로 파일명 저장
            String fileName = prefix + "_" + UUID.randomUUID() + extension;
            String filePath = Paths.get(UPLOAD_DIR, fileName).toString();

            // 폴더가 없으면 생성
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 파일 저장
            File destFile = new File(filePath);
            file.transferTo(destFile);

            return "/static/images/" + filePath;  // DB에 저장할 파일명 반환
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    /** 아마존 S3 파일 저장 **/
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucketName;
//
//    public UserProfileService(AmazonS3 amazonS3) {
//        this.amazonS3 = amazonS3;
//    }
//
//    public String uploadFileToS3(MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            return null;
//        }
//        try {
//            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), null));
//
//            return amazonS3.getUrl(bucketName, fileName).toString(); // S3 URL 반환
//        } catch (IOException e) {
//            throw new RuntimeException("S3 파일 업로드 실패", e);
//        }
//    }
}
