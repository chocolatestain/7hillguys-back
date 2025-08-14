package com.shinhan.peoch.lifecycleincome.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetAmountRequestDTO {
    private Integer monthlyAmount;
    private Integer period;
    private Long totalAmount;
    private Double refundRate;
}
