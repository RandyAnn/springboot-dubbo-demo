package com.example.common.exception;

import com.example.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理自定义业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        logger.error("Business exception occurred: {}", ex.getMessage(), ex);
        // 假设 BusinessException 中的 code 就是 HTTP 状态码
        ApiResponse<?> response = ApiResponse.error(ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getCode()).body(response);
    }

    // 处理其它所有异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
        logger.error("System internal error occurred", ex);
        ApiResponse<?> response = ApiResponse.error(500, "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

