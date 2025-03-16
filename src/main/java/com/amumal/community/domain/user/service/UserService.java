package com.amumal.community.domain.user.service;

import com.amumal.community.domain.user.dto.request.PasswordUpdateRequest;
import com.amumal.community.domain.user.dto.request.UserUpdateRequest;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;

public interface UserService {

    void updateProfile(UserUpdateRequest request);

    void updatePassword(PasswordUpdateRequest request);

    UserInfoResponse getUserInfo(Long userId);

    void deleteUser(Long userId);

    User findById(Long userId);
}