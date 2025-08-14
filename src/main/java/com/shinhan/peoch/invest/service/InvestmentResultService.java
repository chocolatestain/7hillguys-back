package com.shinhan.peoch.invest.service;

import com.shinhan.entity.InvestmentEntity;
import com.shinhan.entity.InvestmentStatus;
import com.shinhan.repository.InvestmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvestmentResultService {
    private final InvestmentRepository investmentRepository;

    @Transactional(readOnly = true)
    public String getInvestmentStatus(Integer userId) {
        Optional<InvestmentEntity> investment = investmentRepository.findByUserId(userId);

        //투자 정보를 찾았는지 확인하는 로그 추가
        if (investment.isPresent()) {
            System.out.println("[백엔드] 투자 정보 찾음: " + investment.get());
            return investment.get().getStatus().name();
        } else {
            System.out.println("[백엔드] 투자 정보를 찾을 수 없음. userId: " + userId);
            return "NOT_FOUND"; // 투자 정보 없음
        }
    }


    @Transactional
    public void approveInvestmentByUser(Long userId) {
        InvestmentEntity investment = investmentRepository.findByUserId(userId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("투자 정보를 찾을 수 없습니다."));

        investment.setStatus(InvestmentStatus.승인);
        investmentRepository.save(investment);
    }

    public void rejectInvestmentByUser(Long userId) {
        InvestmentEntity investment = investmentRepository.findByUserId(userId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("투자 정보를 찾을 수 없습니다."));

        investment.setStatus(InvestmentStatus.거절);
        investmentRepository.save(investment);
    }
}