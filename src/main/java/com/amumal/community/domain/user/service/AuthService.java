package com.amumal.community.domain.user.service;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.entity.User;

public interface AuthService {
    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 ID
     */
    Long signup(SignupRequest request);

    /**
     * 로그인
     * @param request 로그인 요청 DTO
     * @return 로그인에 성공한 User 엔티티
     */
    User login(LoginRequest request);
}
