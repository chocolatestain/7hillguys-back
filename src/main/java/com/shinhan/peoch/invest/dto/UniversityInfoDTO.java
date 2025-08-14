package com.shinhan.peoch.invest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniversityInfoDTO {

    @JsonProperty("universityName")
    private String universityName;

    @JsonProperty("major")
    private String major;
}
