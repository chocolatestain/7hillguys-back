package com.shinhan.peoch.lifecycleincome.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpectedIncomeDTO {
    Integer userId;
    Integer userProfileId;
    String expectedIncome; // JSON 형식의 문자열
}

