package com.amumal.community.global.enums;

import org.springframework.http.HttpStatus;

public enum CustomResponseStatus {
    // 성공 코드
    SUCCESS("success", HttpStatus.OK),

    // 인증 관련 오류
    UNAUTHORIZED("unauthorized", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_REQUEST("unauthorized_request", HttpStatus.FORBIDDEN),

    // 리소스 오류
    NOT_FOUND("not_found", HttpStatus.NOT_FOUND),

    // 요청 오류
    BAD_REQUEST("bad_request", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("validation_failed", HttpStatus.BAD_REQUEST),

    // 서버 오류
    SERVER_ERROR("server_error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus httpStatus;

    CustomResponseStatus(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}