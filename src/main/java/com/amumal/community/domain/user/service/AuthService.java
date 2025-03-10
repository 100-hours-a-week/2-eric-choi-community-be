package com.amumal.community.domain.user.service;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.entity.User;

public interface AuthService {

    Long signup(SignupRequest request);

    User login(LoginRequest request);
}
