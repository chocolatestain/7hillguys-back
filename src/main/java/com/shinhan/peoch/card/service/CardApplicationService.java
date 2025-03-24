package com.shinhan.peoch.card.service;

import com.shinhan.entity.CardDesignEntity;
import com.shinhan.entity.CardEntity;
import com.shinhan.entity.CardStatus;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.card.dto.CardRequestDTO;
import com.shinhan.peoch.design.dto.CardDesignDTO;
import com.shinhan.peoch.security.SecurityUser;
import com.shinhan.repository.CardDesignRepository;
import com.shinhan.repository.CardRepository;
import com.shinhan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardApplicationService {

    private final CardRepository cardRepository;
    private final CardDesignRepository cardDesignRepository; // 카드 디자인 저장용 Repository
    private final UserRepository userRepository;

    @Transactional
    public void createCardApplication(long userid, CardRequestDTO cardDTO, CardDesignDTO cardDesignDTO, MultipartFile imageFile) {
        // (1) 카드번호 생성 (중복 체크 생략)
        String cardNumber = generateRandomCardNumber();

        // (2) 사용자 조회 (예시: userId=16L로 고정)
        UserEntity user = userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));



        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM");

// 한국 표준시로 오늘 날짜를 가져옴
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate fiveYearsLater = today.plusYears(5);

        CardEntity newCard = CardEntity.builder()
                .cardNumber(cardNumber)
                .enName(cardDTO.getEnglishName())
                .password(cardDTO.getPin())
                .cvc(generateCVC())
                .issuedDate(today.format(formatter))          // 예: 오늘이 2025-03-19이면 "25/03"
                .expirationDate(fiveYearsLater.format(formatter)) // 5년 뒤, 예: "30/03"
                .status(CardStatus.ACTIVE)
                .monthlyAllowance(cardDTO.getMonthlyAllowance())
                .tempAllowance(0)
                .monthlySpent(0)
                .user(user)
                .build();

        // (4) 카드 저장 후 DB가 생성한 cardId가 포함된 엔티티 반환
        CardEntity savedCard = cardRepository.save(newCard);

        // (5) 이미지 파일 처리: imageFile이 있다면 파일 저장 후 URL 반환, 없다면 기존 DTO의 값을 사용
        String imageUrl = processImage(imageFile, cardDesignDTO.getUsername());

        // (6) 카드 디자인 엔티티 생성 (카드 ID를 연관관계로 설정)
        CardDesignEntity designEntity = CardDesignEntity.builder()
                .username(cardDesignDTO.getUsername())
                .layoutId(cardDesignDTO.getLayoutId())
                .letterColor(cardDesignDTO.getLetterColor())
                .bgImageUrl(imageUrl != null ? imageUrl : cardDesignDTO.getBgImageUrl())
                .cardBackColor(cardDesignDTO.getCardBackColor())
                .logoGrayscale(cardDesignDTO.isLogoGrayscale())
                .build();

        // (7) 카드 디자인 엔티티에 카드와의 연관관계 설정
        designEntity.setCard(savedCard);

        // (8) 카드 디자인 엔티티 저장
        cardDesignRepository.save(designEntity);
    }

    private String generateRandomCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }

    private String generateCVC() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }

    // 이미지 파일을 처리하여 저장하고, 접근 가능한 URL 반환 (실제 구현은 환경에 맞게 조정)
    private String processImage(MultipartFile imageFile, String username) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        try {
            String uploadDir = System.getProperty("user.dir")
                    + File.separator + "src"
                    + File.separator + "main"
                    + File.separator + "resources"
                    + File.separator + "design"
                    + File.separator + "image";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalFilename = imageFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "username" + "_" + System.currentTimeMillis() + extension;
            File destFile = new File(dir, filename);
            imageFile.transferTo(destFile);
            return "image/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("이미지 저장 실패");
        }
    }
}
