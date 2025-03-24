package com.shinhan.peoch.lifecycleincome.controller;


import com.shinhan.entity.ExpectedIncomeEntity;
import com.shinhan.peoch.lifecycleincome.service.ExpectedIncomeService;
import com.shinhan.peoch.lifecycleincome.service.ExpectedValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExpectedValueController {

    @Autowired
    private ExpectedValueService expectedValueService;
    @Autowired
    private ExpectedIncomeService expectedIncomeService;

    @GetMapping("/expectedvalue/{userId}")
    public Double getExpectedValue(@PathVariable Integer userId) {
//        System.out.println(expectedValueService.calculatePresentValue(userId));
        return expectedValueService.calculatePresentValue(userId)*5;
    }
    @GetMapping("/expectedincome/{userProfileId}")
    public ResponseEntity<List<ExpectedIncomeEntity>> getExpectedIncomes(
            @PathVariable Integer userProfileId) {

        List<ExpectedIncomeEntity> incomes = expectedIncomeService.findByUserProfile_UserProfileId(userProfileId);
        return ResponseEntity.ok(incomes);
    }

}
