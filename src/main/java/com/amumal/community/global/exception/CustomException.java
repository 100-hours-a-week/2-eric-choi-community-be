package com.amumal.community.global.exception;

import com.amumal.community.global.enums.CustomResponseStatus;

public class CustomException extends RuntimeException {
    private final CustomResponseStatus status;

    public CustomException(CustomResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }

    public CustomResponseStatus getStatus() {
        return status;
    }
}
