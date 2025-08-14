package com.shinhan.peoch.lifecycleincome.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO<T> {
    private boolean success;
    private T data;
    private String message;
    private String errorCode;

    // 성공햇을떄 사용 데이터 리턴 메세지 null
    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>(true, data, null, null);
    }

    // 실패할때 사용 데이터 null 메세지 리턴
    public static <T> ApiResponseDTO<T> error(String message, String errorCode) {
        return new ApiResponseDTO<>(false, null, message, errorCode);
    }

}