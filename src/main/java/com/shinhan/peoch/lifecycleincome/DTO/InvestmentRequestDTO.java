package com.shinhan.peoch.lifecycleincome.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentRequestDTO {
    private Integer userId;
    private Integer investAmount;
}
