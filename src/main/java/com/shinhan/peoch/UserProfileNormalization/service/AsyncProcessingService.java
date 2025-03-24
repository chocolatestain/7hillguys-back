package com.shinhan.peoch.UserProfileNormalization.service;

import com.shinhan.peoch.UserProfileNormalization.perplexity.UserProfileNormalizationPerplexityService;
import com.shinhan.peoch.lifecycleincome.service.InvestmentService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncProcessingService {

    private final UserProfileNormalizationPerplexityService userProfileNormalizationPerplexityService;
    private final InvestmentService investmentService;

    // 생성자 주입
    public AsyncProcessingService(
            UserProfileNormalizationPerplexityService userProfileNormalizationPerplexityService,
            InvestmentService investmentService) {
        this.userProfileNormalizationPerplexityService = userProfileNormalizationPerplexityService;
        this.investmentService = investmentService;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Void> profileToExpectedIncome(int userProfileId, int userId) {
        try {
            System.out.println("비동기 처리 시작: " + Thread.currentThread().getName());

            userProfileNormalizationPerplexityService.normalizeAndSaveUserProfile(userProfileId);
            System.out.println("normalizeAndSaveUserProfile 완료");

            userProfileNormalizationPerplexityService.normalizeProfileToExpectedIncome(userProfileId);
            System.out.println("normalizeProfileToExpectedIncome 완료");

            investmentService.createOrUpdateInvestment(userId,userProfileId);
            System.out.println("createOrUpdateInvestment 완료");

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            System.err.println("비동기 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }
    }
}