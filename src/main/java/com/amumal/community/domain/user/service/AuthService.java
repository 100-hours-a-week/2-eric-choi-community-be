package com.amumal.community.domain.user.service;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.dto.response.AuthResponse;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    Long signup(SignupRequest request, MultipartFile profilImage);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    UserInfoResponse convertToUserResponse(User user);
}