package com.shinhan.peoch.invest.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserProfileFileDTO {
    private MultipartFile universityCertificate;
    private MultipartFile studentCardFile;
    private MultipartFile[] certificationFiles;
    private MultipartFile familyCertificate;
    private MultipartFile criminalRecordFile;
}