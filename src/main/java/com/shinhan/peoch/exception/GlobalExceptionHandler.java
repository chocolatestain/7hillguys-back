package com.shinhan.peoch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    //에러코드가 안보여서 주석처리했어요 문제 된다면 삭제 부탁드려요!
    /**
     * CustomException 처리 (예외별로 상태 코드 설정 가능)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ErrorResponseDTO(e.getMessage(), e.getStatus().value()));
    }
    /**
     * CustomException 처리 (예외별로 상태 코드 설정 가능)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(CustomException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ErrorResponseDTO(e.getMessage(), e.getStatus().value()));
    }

    /**
     * 401 Unauthorized (인증 실패)
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO("인증 실패: " + e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    /**
     * 403 Forbidden (권한 없음)
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDTO("접근 권한이 없습니다.", HttpStatus.FORBIDDEN.value()));
    }

    /**
     * 404 Not Found (리소스 없음)
     */
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(ResponseStatusException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("리소스를 찾을 수 없습니다: " + e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    /**
     * 500 Internal Server Error (예상치 못한 서버 에러)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
