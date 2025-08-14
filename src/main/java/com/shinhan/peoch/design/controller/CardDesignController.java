package com.shinhan.peoch.design.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;

import com.shinhan.peoch.design.dto.CardDesignDTO;
import com.shinhan.entity.CardDesignEntity;
import com.shinhan.peoch.design.service.CardDesignService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/card/design")
@RequiredArgsConstructor
public class CardDesignController {

    private final CardDesignService cardDesignService;

@PostMapping("/insert")
public ResponseEntity<String> createCardDesign(
    @RequestPart("cardDesignDTO") CardDesignDTO cardDesignDTO,
    @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
    // 사진 저장 /////////////////////////////////////////////////////
    String imageUrl = null;
    if (imageFile != null && !imageFile.isEmpty()) {
        try {
             
            // src/main/resources/design/image 폴더에 이미지 저장
            String uploadDir = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "design" + File.separator + "image";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        
            String originalFilename = imageFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
           
            String filename = cardDesignDTO.getUsername() + "_" + System.currentTimeMillis() + extension;
            
            File destFile = new File(dir, filename);
            imageFile.transferTo(destFile);
            
            imageUrl = "image/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("이미지 저장 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // 사진 저장 ///////////////////////////////////////////////////// 추후 이미지 호스팅 시작 후 삭제
 
    CardDesignEntity entity; 
    try {
        entity = CardDesignEntity.builder()
            .username(cardDesignDTO.getUsername())
            .layoutId(cardDesignDTO.getLayoutId())
            .letterColor(cardDesignDTO.getLetterColor()) 
            .bgImageUrl(imageUrl != null ? imageUrl : cardDesignDTO.getBgImageUrl())
            .cardBackColor(cardDesignDTO.getCardBackColor())
            .logoGrayscale(cardDesignDTO.isLogoGrayscale())
            .build();
    
    } catch (Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>("카드 생성 실패", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    cardDesignService.registerCardDesign(entity);
    return new ResponseEntity<>("카드 생성 완료", HttpStatus.CREATED);
}

    @GetMapping("/")
    public ResponseEntity<List<CardDesignEntity>> getAllCardDesigns() {
        List<CardDesignEntity> list = cardDesignService.getAllCards();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDesignEntity> getCardDesignById(@PathVariable("id") int id) {
        Optional<CardDesignEntity> entity = cardDesignService.getDesignById(id);
        return entity.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCardDesign(@PathVariable("id") int id) {
        boolean deleted = cardDesignService.deleteCardById(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
