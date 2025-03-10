package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.AuthService;
import com.amumal.community.global.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/new")
    public ResponseEntity<ApiResponse<Long>> register(@Validated @RequestBody SignupRequest signupRequest) {
        Long userId = authService.signup(signupRequest);
        ApiResponse<Long> response = new ApiResponse<>("register_success", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth")
    public ResponseEntity<ApiResponse<Void>> login(@Validated @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        // 로그인 검증 (비밀번호 일치 여부 등)
        User user = authService.login(loginRequest);

        // 세션 생성 및 사용자 정보 저장
        HttpSession session = request.getSession(true);
        session.setAttribute("USER", user);

        ApiResponse<Void> response = new ApiResponse<>("login_success", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        ApiResponse<Void> response = new ApiResponse<>("logout_success", null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
