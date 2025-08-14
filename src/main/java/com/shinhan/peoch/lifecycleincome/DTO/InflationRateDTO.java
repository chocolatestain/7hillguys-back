package com.shinhan.peoch.lifecycleincome.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InflationRateDTO {
    private Integer year;
    private Map<Integer, Double> rates; // Key: 지원기간(년)

    public Double getRateByPeriod(Integer period) {
        if (period <= 5) return rates.getOrDefault(period, rates.get(5));
        if (period <= 9) return rates.get(5);
        if (period <= 19) return rates.get(10);
        if (period <= 29) return rates.get(20);
        if (period <= 49) return rates.get(30);
        return rates.get(50);
    }
}