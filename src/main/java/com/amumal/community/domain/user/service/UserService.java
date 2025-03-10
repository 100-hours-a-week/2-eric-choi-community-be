package com.amumal.community.domain.user.service;

import com.amumal.community.domain.user.dto.request.PasswordUpdateRequest;
import com.amumal.community.domain.user.dto.request.UserUpdateRequest;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;

public interface UserService {

    /**
     * 프로필 업데이트를 수행합니다.
     * @param request 사용자 프로필 업데이트 요청 DTO
     */
    void updateProfile(UserUpdateRequest request);

    /**
     * 비밀번호 업데이트를 수행합니다.
     * @param request 비밀번호 업데이트 요청 DTO
     */
    void updatePassword(PasswordUpdateRequest request);

    /**
     * 사용자 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return 사용자 정보 응답 DTO
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 회원 탈퇴(논리 삭제)를 수행합니다.
     * @param userId 사용자 ID
     */
    void deleteUser(Long userId);
}
