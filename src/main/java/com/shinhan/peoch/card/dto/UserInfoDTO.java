package com.shinhan.peoch.card.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private String name;
    private String phone;
    private String email;
    private String address;
    private Integer monthlyAllowance;
    private Integer maxInvestment;
    private LocalDate endDate;
}

