package com.shinhan.peoch.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class PosPaymentRequest {
    private String cardNumber;
    private Integer amount;
    private Long storeId;
    private Integer installmentMonth;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate; // LocalDateTime 사용

}
