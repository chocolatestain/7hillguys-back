package com.shinhan.peoch.card;

import com.shinhan.entity.MyBenefitStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestDTO {
    private Long benefitId;

    private Long cardId;

    private Integer usedCount;

    private MyBenefitStatus status;

}
