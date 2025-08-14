package com.shinhan.peoch.payment;

import lombok.Data;

@Data
public class PosRefundRequest {
    private Long paymentId;
    private String cardNumber;
    private Long storeId;

}
