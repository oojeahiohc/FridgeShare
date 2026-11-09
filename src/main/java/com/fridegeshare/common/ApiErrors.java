package com.fridegeshare.common;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// API 예외의 JSON 변환
@RestControllerAdvice(annotations = RestController.class)
class ApiErrors {
    private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, String>> applicationError(ApiException error) {
        return ResponseEntity.status(error.status())
                .body(Map.of("code", error.code(), "message", error.getMessage()));
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    ResponseEntity<Map<String, String>> invalidRequest(Exception error) {
        return applicationError(ApiException.invalid("요청 값과 형식을 확인하세요."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> unexpected(Exception error) {
        // 상세 예외는 서버 로그에만 기록
        log.error("API request failed", error);
        return ResponseEntity.internalServerError()
                .body(Map.of("code", "INTERNAL_ERROR", "message", "요청을 처리하지 못했습니다."));
    }
}