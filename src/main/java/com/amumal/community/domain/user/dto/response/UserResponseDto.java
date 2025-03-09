package com.amumal.community.domain.user.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {

    private Long userId;
    private String message;

    // 기본 생성자, 메시지를 반환하는 응답을 생성하는 데 사용
    public UserResponseDto(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }
}
