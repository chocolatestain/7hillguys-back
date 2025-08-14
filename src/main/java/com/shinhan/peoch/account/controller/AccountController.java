package com.shinhan.peoch.account.controller;

import com.shinhan.peoch.account.dto.BillDTO;
import com.shinhan.peoch.account.dto.PaymentCalculationDTO;
import com.shinhan.peoch.account.entity.AccountEntity;
import com.shinhan.peoch.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    //현재 소득 조회
    @PostMapping("/show/income")
    public ResponseEntity<String> showIncome() {
        String income = accountService.getIncome();
        return ResponseEntity.ok(income);
    }

    //납부 계좌 관리 (등록, 수정)
    @PostMapping("/manage")
    public ResponseEntity<List<AccountEntity>> manageAccount(@RequestBody AccountEntity account) {
        List<AccountEntity> accountList = accountService.getAllAccounts();
        return ResponseEntity.ok(accountList);
    }

    //오픈 뱅킹 동의
    @GetMapping("/agree/openbanking")
    public ResponseEntity<String> agreeOpenBanking() {
        String result = accountService.agreeOpenBanking();
        return ResponseEntity.ok(result);
    }

    //다른 금융 계좌 등록 동의
    @GetMapping("/agree/otheraccount")
    public ResponseEntity<String> agreeOtherAccount() {
        String result = accountService.agreeOtherAccount();
        return ResponseEntity.ok(result);
    }

    //다른 금융 계좌 등록
    @PostMapping("/other")
    public ResponseEntity<AccountEntity> registerOtherAccount(@RequestBody AccountEntity account) {
        AccountEntity savedAccount = accountService.registerOtherAccount(account);
        return ResponseEntity.ok(savedAccount);
    }

    //납부 내역 조회
    @PostMapping("/check")
    public ResponseEntity<List<AccountEntity>> getAllAccounts() {
        List<AccountEntity> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    //긍정 요인 시각화
    @PostMapping("/see/positive")
    public ResponseEntity<String> visualizePositiveFactors() {
        String result = accountService.visualizePositiveFactors();
        return ResponseEntity.ok(result);
    }

    // 청구서 조회
    @PostMapping("/show/bill")
    public ResponseEntity<BillDTO> showBill(Long userId) {
        BillDTO bills = accountService.showBill(userId);
        return ResponseEntity.ok(bills);
    }

    //납부 금액 산정
    @PostMapping("/cal")
    public ResponseEntity<Integer> calculatePaymentAmount(
            @RequestBody PaymentCalculationDTO request
    ) {
        // JSON으로 넘어온 userId, userMonthlyIncome를 꺼냄
        int result = accountService.calculatePaymentAmount(
                request.getUserId(),
                request.getUserMonthlyIncome()
        );

        return ResponseEntity.ok(result);
    }


    // 납부 금액 산정 결과 조회
    @GetMapping("/result")
    public ResponseEntity<String> getPaymentCalculationResult() {
        String result = accountService.getPaymentCalculationResult();
        return ResponseEntity.ok(result);
    }



}
