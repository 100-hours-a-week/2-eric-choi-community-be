package com.amumal.community.global.exception;

import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.global.enums.CustomResponseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        CustomResponseStatus status = ex.getStatus();
        ApiResponse<Void> response = new ApiResponse<>(status.getCode(), null);
        return ResponseEntity.status(status.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        ex.printStackTrace();
        ApiResponse<Void> response = new ApiResponse<>("server_error", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}