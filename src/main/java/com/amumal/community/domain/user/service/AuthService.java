package com.amumal.community.domain.user.service;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.dto.response.AuthResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    Long signup(SignupRequest request, MultipartFile profilImage);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}