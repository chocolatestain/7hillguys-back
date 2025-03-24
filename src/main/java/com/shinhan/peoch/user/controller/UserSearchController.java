package com.shinhan.peoch.user.controller;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shinhan.entity.InvestmentEntity;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.card.CardDataMapResponseDTO;
import com.shinhan.peoch.card.CardDataMapService;
import com.shinhan.peoch.card.CardDataTotalResponseDTO;
import com.shinhan.peoch.card.CardDataTotalService;
import com.shinhan.peoch.security.SecurityUser;
import com.shinhan.peoch.user.dto.UserInfoDTO;
import com.shinhan.peoch.user.service.UserSearchService;
import com.shinhan.repository.InvestmentRepository;
@RestController
@RequestMapping("/api/user")
public class UserSearchController {
    @Autowired
    private InvestmentRepository investmentRepository;
    @Autowired
    private UserSearchService UserSearchService;

    @Autowired
    private CardDataTotalService cardDataTotalService;
    
    @Autowired
    private CardDataMapService cardDataMapService;
   
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("query") String query) {
        if(query == null || query.trim().isEmpty()){
            return new ResponseEntity<>("검색어 없음", HttpStatus.BAD_REQUEST);
        }
        try {
            List<UserEntity> users = UserSearchService.searchUsersByName(query);
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("서버 내부 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfos(@RequestParam("userid") Long userid) {
        if (userid == null) {
            return new ResponseEntity<>("사용자 아이디 필요.", HttpStatus.BAD_REQUEST);
        }
        try {
            UserInfoDTO dto = UserSearchService.getAdminUserFlatDetail(userid);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("서버 내부 에러 발생.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/expectedincome")
    public String getExpectedIncomes(
            @AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null) {
            throw new RuntimeException("인증되지 않은 사용자.");
        }
        InvestmentEntity investment = investmentRepository.findInvestmentByUserId(securityUser.getUserId());
        String expectedincome = investment.getExpectedIncome();
        return expectedincome;
    }
    @GetMapping("/age")
    public int getUserAge(@AuthenticationPrincipal SecurityUser securityUser){
        if (securityUser == null) {
            throw new RuntimeException("인증되지 않은 사용자.");
        }
        LocalDate birthdate = securityUser.getBirthdate();
        int age = Period.between(birthdate, LocalDate.now()).getYears();
        return age;
    }
    @GetMapping("/usertype")
    public String getUserType(@AuthenticationPrincipal SecurityUser securityUser){
        if (securityUser == null) {
            throw new RuntimeException("인증되지 않은 사용자.");
        }
        String userType = securityUser.getRole();
        return userType;
    }
 
    @GetMapping("/cardDataTotal")
    public CardDataTotalResponseDTO getCardDataTotal(@RequestParam("userid") Long userid,
                                              @RequestParam String date) {

        if (userid == null) {
            return null;
        }
        return cardDataTotalService.getCardDataTotal(userid, date);
    } 

    @GetMapping("/cardDataMap")
    public CardDataMapResponseDTO getCardDataMap(@RequestParam("userid") Long userid,
                                              @RequestParam String date) {
        if (userid == null) {
            return null;
        }
        return cardDataMapService.getCardDataMap(userid, date);
    }


}