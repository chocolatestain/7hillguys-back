package com.shinhan.peoch.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDataTotalResponseDTO {

    // Integer만 반환
    private Integer weekCurrentTotal;
    private Integer weekPreviousTotal;
    private Integer monthCurrentTotal;
    private Integer monthPreviousTotal;
    private Integer yearCurrentTotal;
    private Integer yearPreviousTotal;
    private Integer avgWeekCurrentTotal;
    private Integer avgWeekPreviousTotal;
    private Integer avgMonthCurrentTotal;
    private Integer avgMonthPreviousTotal;
    private Integer avgYearCurrentTotal;
    private Integer avgYearPreviousTotal;
}
