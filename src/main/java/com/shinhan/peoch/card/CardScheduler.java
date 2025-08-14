package com.shinhan.peoch.card;

import com.shinhan.peoch.payment.PaymentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CardScheduler {
    @PersistenceContext
    private EntityManager entityManager;

    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 00:00:00 실행
    @Transactional
    public void callCardMonthlyProcedure() {
        try {
            // 매월 1일 프로시저 실행
            entityManager.createNativeQuery("CALL card_monthly_procedure();").executeUpdate();
        } catch (Exception e) {
            System.err.println("스케줄러 실행 중 오류 발생: " + e.getMessage());
        }
    }
}
