package com.shinhan.peoch.education.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor@NoArgsConstructor
@Data@Builder
public class UserProfile2DTO {
    private Long userId;
    private String letter;
    private String languageScore;
    private String certification;
    private String internship;
    private String grade;
}
