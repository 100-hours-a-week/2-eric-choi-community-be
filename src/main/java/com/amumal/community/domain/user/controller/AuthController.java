package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.dto.response.AuthResponse;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.global.config.security.JwtUserDetails;
import com.amumal.community.domain.user.service.AuthService;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.dto.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/auth")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.login(loginRequest);

        // 리프레시 토큰을 HttpOnly 쿠키로 설정
        ResponseCookie cookie = ResponseCookie.from("refresh_token", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서는 true로 설정
                .sameSite("Strict")
                .maxAge(Duration.ofDays(14)) // 리프레시 토큰 유효기간과 일치
                .path("/users/refresh") // 리프레시 엔드포인트에서만 사용 가능
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 응답에서 리프레시 토큰 제거
        authResponse.setRefreshToken(null);

        return ResponseEntity.ok(new ApiResponse<>("login_success", authResponse));
    }

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> register(
            @RequestPart(value = "userInfo") @Validated SignupRequest signupRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        Long userId = authService.signup(signupRequest, profileImage);
        ApiResponse<Long> response = new ApiResponse<>("register_success", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        // 쿠키에서 리프레시 토큰 추출
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("invalid_refresh_token", null));
        }

        try {
            AuthResponse authResponse = authService.refreshToken(refreshToken);

            // 새 리프레시 토큰으로 쿠키 업데이트
            ResponseCookie cookie = ResponseCookie.from("refresh_token", authResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .maxAge(Duration.ofDays(14))
                    .path("/users/refresh")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 응답에서 리프레시 토큰 제거
            authResponse.setRefreshToken(null);

            return ResponseEntity.ok(new ApiResponse<>("token_refresh_success", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("token_refresh_failed", null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // 리프레시 토큰 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .maxAge(0) // 즉시 만료
                .path("/users/refresh")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new ApiResponse<>("logout_success", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        if (userDetails == null) {
            logger.warn("인증되지 않은 사용자의 접근 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );
        }

        User user = userService.findById(userDetails.getId());
        logger.info("현재 로그인된 사용자 정보 조회: {}", user.getEmail());

        // AuthService를 통해 User를 DTO로 변환
        UserInfoResponse userResponse = authService.convertToUserResponse(user);

        ApiResponse<UserInfoResponse> response = new ApiResponse<>("success", userResponse);
        return ResponseEntity.ok(response);
    }

    // 쿠키에서 리프레시 토큰 추출
    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}