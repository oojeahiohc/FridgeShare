package com.fridegeshare.common;

import org.springframework.http.HttpStatus;

// 업무 오류의 HTTP 상태와 코드
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }

    public static ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", message);
    }

    public static ApiException missing() {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 데이터를 찾을 수 없습니다.");
    }
}