package com.shinhan.peoch.design.dto;

import lombok.AllArgsConstructor;
import lombok.*;

@Builder
@NoArgsConstructor 
@AllArgsConstructor
@Getter
@Setter 
@Data
public class CardDesignDTO {
 
    private Integer layoutId;
 
    private String username;
 
    private int letterColor;
 
    private String cardBackColor;
 
    private boolean logoGrayscale;
     
    private String bgImageUrl;
}