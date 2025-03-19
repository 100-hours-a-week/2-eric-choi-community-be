package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.response.AuthResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.security.JwtUserDetails;
import com.amumal.community.domain.user.service.AuthService;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.dto.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<ApiResponse<User>> getCurrentUser(
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        if (userDetails == null) {
            logger.warn("인증되지 않은 사용자의 접근 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );
        }

        User user = userService.findById(userDetails.getId());
        logger.info("현재 로그인된 사용자 정보 조회: {}", user.getEmail());

        // 클라이언트에 반환할 사용자 정보에서 민감한 정보 제거
        User userResponse = sanitizeUserForResponse(user);

        ApiResponse<User> response = new ApiResponse<>("success", userResponse);
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

    /**
     * 클라이언트에 전송하기 전에 사용자 객체에서 민감한 정보를 제거하는 메서드
     */
    private User sanitizeUserForResponse(User user) {
        // Builder 패턴을 사용하여 비밀번호를 제외한 새 User 객체 생성
        return User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                // password는 의도적으로 제외
                .build();
    }
}