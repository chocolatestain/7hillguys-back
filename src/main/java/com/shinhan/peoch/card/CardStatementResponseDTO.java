package com.shinhan.peoch.card;

import lombok.Data;

import java.util.List;

@Data
public class CardStatementResponseDTO {
    private List<CardStatementDTO> statementList;
    private Integer monthlyAllowance;
    private Integer monthlySpent;

}
