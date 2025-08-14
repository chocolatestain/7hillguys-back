package com.shinhan.peoch.account.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class BillDTO {
    private String month;
    private Integer amount;
}
