package com.shinhan.peoch.design.service;

import com.shinhan.entity.CardDesignEntity;
import com.shinhan.repository.CardDesignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardDesignService {

    private final CardDesignRepository cardDesignRepository;

    // 카드 디자인 등록 (DB 저장)
    public CardDesignEntity registerCardDesign(CardDesignEntity cardDesign) {
        return cardDesignRepository.save(cardDesign);
    }

    // 모든 카드 디자인 조회
    public List<CardDesignEntity> getAllCards() {
        return cardDesignRepository.findAll();
    }

    // ID에 따른 카드 디자인 조회
    public Optional<CardDesignEntity> getDesignById(int id) {
        return cardDesignRepository.findById(id);
    }

    // ID에 따른 카드 디자인 삭제
    public boolean deleteCardById(int id) {
        Optional<CardDesignEntity> entity = cardDesignRepository.findById(id);
        if (entity.isPresent()) {
            cardDesignRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
