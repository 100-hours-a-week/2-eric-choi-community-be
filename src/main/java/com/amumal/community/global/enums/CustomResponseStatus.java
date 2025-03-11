package com.amumal.community.global.enums;

public enum CustomResponseStatus {
    UNAUTHORIZED_REQUEST(401, "Unauthorized request"),
    NOT_FOUND(404, "Resource not found"),
    INTERNAL_SERVER_ERROR(500, "Internal server error");

    private final int code;
    private final String message;

    CustomResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
