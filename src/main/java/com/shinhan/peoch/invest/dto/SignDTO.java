package com.shinhan.peoch.invest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignDTO {
    private Integer userId;
    private String base64Signature;
}
