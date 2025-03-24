package com.shinhan.peoch.card.dto;

import com.shinhan.peoch.design.dto.CardDesignDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardRequestDTO {
    private String englishName;
    private String pin;
    private Integer monthlyAllowance;
}
