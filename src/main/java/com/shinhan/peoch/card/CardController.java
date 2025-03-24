package com.shinhan.peoch.card;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.shinhan.peoch.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/card")
public class CardController {

    private final CardService cardService;
    private final CardDataTotalService cardDataTotalService;
    private final CardDataMapService cardDataMapService;

    public CardController(CardService cardService, CardDataTotalService cardDataTotalService,
                          CardDataMapService cardDataMapService) {
        this.cardService = cardService;
        this.cardDataTotalService = cardDataTotalService;
        this.cardDataMapService = cardDataMapService;
    }

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    // 카드 명세서 조회
    @GetMapping("/cardStatement")
    public CardStatementResponseDTO getCardStatement(@CookieValue(value = "jwt", required = false) String jwtToken,
            @RequestParam(required = false) String yearMonth) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }

        // JWT에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userId == null) {
            return null;
        }

        return cardService.getCardStatement(userId, yearMonth);
    }

    // 혜택 명세서 조회
    @GetMapping("/benefitStatement")
    public BenefitStatementResponseDTO getCardPerformance(@CookieValue(value = "jwt", required = false) String jwtToken,
                                                          @RequestParam(required = false) String yearMonth) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }

        // JWT에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userId == null) {
            return null;
        }

        return cardService.getCardPerformance(userId, yearMonth);
    }

    // 전체 혜택 조회
    @GetMapping("/allBenefitSearch")
    public List<AllBenefitDTO> getAllBenefit(@CookieValue(value = "jwt", required = false) String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }

        // JWT에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userId == null) {
            return null;
        }

        return cardService.getAllBenefit(userId);
    }

    // 관리자 대시보드에서 카드 데이터(Total) 요청
    @GetMapping("/cardDataTotal")
    public CardDataTotalResponseDTO getCardDataTotal(@CookieValue(value = "jwt", required = false) String jwtToken,
                                              @RequestParam String date) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }

        // JWT에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userId == null) {
            return null;
        }

//        CardDataTotalResponseDTO response = cardDataTotalService.getCardDataTotal(userId, date);
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        try {
//            String prettyJson = mapper.writeValueAsString(response);
//            System.out.println(prettyJson);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        return cardDataTotalService.getCardDataTotal(userId, date);
    }

    // 관리자 대시보드에서 카드 데이터(Map) 요청
    @GetMapping("/cardDataMap")
    public CardDataMapResponseDTO getCardDataMap(@CookieValue(value = "jwt", required = false) String jwtToken,
                                              @RequestParam String date) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            return null;
        }

        // JWT에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(jwtToken);
        if (userId == null) {
            return null;
        }

//        CardDataMapResponseDTO response = cardDataMapService.getCardDataMap(userId, date);
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        try {
//            String prettyJson = mapper.writeValueAsString(response);
//            System.out.println(prettyJson);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }


        return cardDataMapService.getCardDataMap(userId, date);
    }

}
