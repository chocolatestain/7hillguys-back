package com.shinhan.peoch.account.service;

import com.shinhan.peoch.account.entity.InvestmentChargeEntity;
import com.shinhan.peoch.account.dto.BillDTO;
import com.shinhan.peoch.account.entity.AccountEntity;
import com.shinhan.peoch.lifecycleincome.service.InvestmentService;
import com.shinhan.repository.AccountRepository;
import com.shinhan.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BillRepository billRepository;
    private final AccountRepository accountRepository;

    private final InvestmentService investmentService;

    public String getIncome() {
        return "현재 소득 정보";
    }

    public AccountEntity manageAccount(AccountEntity account) {

        return accountRepository.save(account);
    }

    public String agreeOpenBanking() {
        return "오픈 뱅킹 동의 완료";
    }

    public String agreeOtherAccount() {
        return "다른 금융 계좌 등록 동의 완료";
    }

    public AccountEntity registerOtherAccount(AccountEntity account) {
        return accountRepository.save(account);
    }

    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    public String visualizePositiveFactors() {
        return "긍정 요인 시각화 데이터";
    }

    public BillDTO showBill(Long userId) {
            InvestmentChargeEntity bill = billRepository.findByUserSalary_UserId(userId).orElseThrow(()->new RuntimeException("청구정보없음"));

            String yyyyMm = bill.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            return new BillDTO(yyyyMm, bill.getInvestChargeAmount());
    }



    public String getPaymentCalculationResult() {
        return "납부 금액 산정 결과";
    }

    public int calculatePaymentAmount(Integer userId, int userMonthlyIncome) {
        // InvestmentService에 정의된 로직을 호출
        return investmentService.calculateRefundAmount(userId, userMonthlyIncome);
    }
}
