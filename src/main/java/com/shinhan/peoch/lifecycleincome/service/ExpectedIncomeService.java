package com.shinhan.peoch.lifecycleincome.service;

import com.shinhan.entity.ExpectedIncomeEntity;
import com.shinhan.repository.ExpectedIncomeRepository;
import com.shinhan.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpectedIncomeService {

    @Autowired
    ExpectedIncomeRepository expectedIncomeRepository;
    @Autowired
    UserProfileRepository userProfileRepository;
    public List<ExpectedIncomeEntity> findByUserProfile_UserProfileId(Integer userProfileId) {
        return expectedIncomeRepository.findByUserProfileId(userProfileId);
    }
}
