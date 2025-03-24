package com.shinhan.peoch.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDataMapResponseDTO {

    // JSON만 반환
    private Map<String, Integer> weeklyCurrentMap;
    private Map<String, Integer> weeklyPreviousMap;
    private Map<String, Integer> monthlyCurrentMap;
    private Map<String, Integer> monthlyPreviousMap;
    private Map<String, Integer> yearlyCurrentMap;
    private Map<String, Integer> yearlyPreviousMap;
    private Map<String, Integer> weeklyCurrentAverageMap;
    private Map<String, Integer> weeklyPreviousAverageMap;
    private Map<String, Integer> monthlyCurrentAverageMap;
    private Map<String, Integer> monthlyPreviousAverageMap;
    private Map<String, Integer> yearlyCurrentAverageMap;
    private Map<String, Integer> yearlyPreviousAverageMap;
}
