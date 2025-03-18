package com.shinhan.peoch.lifecycleincome.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinhan.entity.ExpectedIncomeEntity;
import com.shinhan.entity.InflationRateEntity;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.repository.ExpectedIncomeRepository;
import com.shinhan.repository.InflationRateRepository;
import com.shinhan.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ExpectedValueService {

    @Autowired
    private ExpectedIncomeRepository expectedIncomeRepository;

    @Autowired
    private InflationRateRepository inflationRateRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    public Double calculatePresentValue(Integer userId) {
        UserProfileEntity userProfile = userProfileRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 Profile이 존재하지 않습니다."));;
        List<ExpectedIncomeEntity> incomeEntities = expectedIncomeRepository.findByUserProfileId(userProfile.getUserProfileId());
        if (incomeEntities.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        ExpectedIncomeEntity latestIncomeEntity = incomeEntities.get(incomeEntities.size() - 1);
        Map<Integer, Double> expectedIncome = parseJsonToMap(latestIncomeEntity.getExpectedIncome());

        InflationRateEntity inflationRateEntity = inflationRateRepository.findByYear(2025);
        Map<Integer, Double> inflationRates = parseJsonToMap(inflationRateEntity.getInflationRate());

        double totalPresentValue = 0.0;
        for (Map.Entry<Integer, Double> entry : expectedIncome.entrySet()) {
            int year = entry.getKey();
            double income = entry.getValue();
            double discountRate = getDiscountRate(year, inflationRates);
            totalPresentValue += calculatePresentValueForIncome(income, year, discountRate);
        }

        return totalPresentValue;
    }
    //생애 주기 소득 총합
    public Double calculateTotalExpectedIncome(Integer grantId) {
        List<ExpectedIncomeEntity> incomeEntities = expectedIncomeRepository.findByUserProfileId(grantId);
        if (incomeEntities.isEmpty()) {
            throw new IllegalArgumentException("유저가 없어용");
        }

        ExpectedIncomeEntity latestIncomeEntity = incomeEntities.get(incomeEntities.size() - 1);
        Map<Integer, Double> expectedIncome = parseJsonToMap(latestIncomeEntity.getExpectedIncome());
        return expectedIncome.values().stream().mapToDouble(Double::doubleValue).sum();
    }




    private double getDiscountRate(int year, Map<Integer, Double> inflationRates) {
        if (year <= 5) return inflationRates.getOrDefault(year, inflationRates.get(5));
        if (year <= 9) return inflationRates.get(5);
        if (year <= 19) return inflationRates.get(10);
        if (year <= 29) return inflationRates.get(20);
        if (year <= 49) return inflationRates.get(30);
        return inflationRates.get(50);
    }

    private Map<Integer, Double> parseJsonToMap(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonString, new TypeReference<Map<Integer, Double>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
    private double calculatePresentValueForIncome(double income, int year, double discountRate) {
        return income / Math.pow(1 + discountRate / 100, year);
    }
}
