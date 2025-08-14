package com.shinhan.peoch.account.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCalculationDTO {
    private Integer userId;
    private int userMonthlyIncome;
}
