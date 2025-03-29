package com.amumal.community.global.exception;

import com.amumal.community.global.enums.CustomResponseStatus;

public class CustomException extends RuntimeException {
    private final CustomResponseStatus status;

    // 기본 생성자: status에 정의된 코드(getCode())를 메시지로 사용
    public CustomException(CustomResponseStatus status) {
        super(status.getCode());
        this.status = status;
    }

    // 커스텀 메시지를 함께 전달할 수 있는 생성자
    public CustomException(CustomResponseStatus status, String message) {
        super(message);
        this.status = status;
    }

    public CustomResponseStatus getStatus() {
        return status;
    }
}
