package com.amumal.community.global.exception;

import com.amumal.community.global.enums.CustomResponseStatus;

public class CustomException extends RuntimeException {
    private final CustomResponseStatus status;

    public CustomException(CustomResponseStatus status) {
        super(status.getCode()); // getMessage() 대신 getCode() 사용
        this.status = status;
    }

    public CustomResponseStatus getStatus() {
        return status;
    }

    public CustomResponseStatus getErrorStatus() {
        return status;
    }
}