package com.amumal.community.global.enums;

public enum CustomResponseStatus {
    UNAUTHORIZED_REQUEST(401, "Unauthorized request"),
    MEMBER_NOT_EXIST(404, "Member does not exist"),
    // 필요에 따라 다른 상태 코드를 추가하세요.
    ;

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
