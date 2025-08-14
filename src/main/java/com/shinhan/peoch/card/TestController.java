package com.shinhan.peoch.card;

import com.shinhan.entity.*;
import com.shinhan.peoch.auth.entity.UserEntity;
import com.shinhan.peoch.payment.PaymentService;
import com.shinhan.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class TestController {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final StoreRepository storeRepository;
    private final BenefitRepository benefitRepository;
    private final MyBenefitRepository myBenefitRepository;

    public TestController(CardRepository cardRepository, UserRepository userRepository, PaymentService paymentService,
                          StoreRepository storeRepository, BenefitRepository benefitRepository,
                          MyBenefitRepository myBenefitRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.storeRepository = storeRepository;
        this.benefitRepository = benefitRepository;
        this.myBenefitRepository = myBenefitRepository;
    }

    @PostMapping("/cardUserInsert")
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        UserEntity savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/cardInsert")
    public ResponseEntity<CardEntity> createCard(@RequestBody CardEntity card) {
        CardEntity savedCard = cardRepository.save(card);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCard);
    }

    @PostMapping("/storeInsert")
    public ResponseEntity<StoreEntity> createStore(@RequestBody StoreEntity store) {
        StoreEntity savedStore = storeRepository.save(store);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStore);
    }

    @PostMapping("/benefitInsert")
    public ResponseEntity<BenefitEntity> createBenefit(@RequestBody BenefitEntity benefit) {
        BenefitEntity savedBenefit = benefitRepository.save(benefit);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBenefit);
    }

    @PostMapping("/myBenefitInsert")
    public ResponseEntity<String> createMyBenefit(@RequestBody TestDTO request) {
        MyBenefitId myBenefitId = MyBenefitId.builder()
                .benefitId(request.getBenefitId())
                .cardId(request.getCardId())
                .build();

        BenefitEntity benefit = benefitRepository.findById(request.getBenefitId())
                .orElseThrow(() -> new RuntimeException("Benefit not found with ID: " + request.getBenefitId()));
        CardEntity card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + request.getCardId()));

        MyBenefitEntity myBenefit = MyBenefitEntity.builder()
                .myBenefitId(myBenefitId)
                .usedCount(request.getUsedCount())
                .status(request.getStatus())
                .benefit(benefit)
                .card(card)
                .build();

        myBenefitRepository.save(myBenefit);
        return ResponseEntity.status(HttpStatus.CREATED).body("save OK");
    }

    @PostMapping("/storeBulkInsert")
    public ResponseEntity<List<StoreEntity>> createStores(@RequestBody List<StoreEntity> stores) {
        List<StoreEntity> savedStores = storeRepository.saveAll(stores);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStores);
    }

    @PostMapping("/bulkInsert")
    public ResponseEntity<List<BenefitEntity>> createBenefits(@RequestBody List<BenefitEntity> benefits) {
        List<BenefitEntity> savedBenefits = benefitRepository.saveAll(benefits);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBenefits);
    }

}
