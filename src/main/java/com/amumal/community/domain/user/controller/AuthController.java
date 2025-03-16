package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.AuthService;
import com.amumal.community.domain.user.service.UserService;  // 추가: 사용자 조회를 위한 서비스
import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.global.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final UserService userService;  // 추가: 사용자 조회를 위한 서비스

    @PostMapping("/new")
    public ResponseEntity<ApiResponse<Long>> register(@Validated @RequestBody SignupRequest signupRequest) {
        Long userId = authService.signup(signupRequest);
        ApiResponse<Long> response = new ApiResponse<>("register_success", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth")
    public ResponseEntity<ApiResponse<User>> login(@Validated @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        // 로그인 처리
        User user = authService.login(loginRequest);

        // 세션에 사용자 정보 저장
        SessionUtil.setLoggedInUser(request, user);
        logger.info("로그인 성공: {}, 세션 ID: {}", user.getEmail(), SessionUtil.getSessionId(request));

        // 클라이언트에 반환할 사용자 정보에서 민감한 정보 제거
        User userResponse = sanitizeUserForResponse(user);

        ApiResponse<User> response = new ApiResponse<>("login_success", userResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        // 세션 무효화
        SessionUtil.invalidateSession(request);
        logger.info("로그아웃 성공");

        ApiResponse<Void> response = new ApiResponse<>("logout_success", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(HttpServletRequest request) {
        // 세션에서 로그인된 사용자 ID 가져오기
        Long userId = SessionUtil.getLoggedInUserId(request);

        if (userId == null) {
            logger.warn("인증되지 않은 사용자의 접근 시도");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );
        }

        // ID로 사용자 정보 조회
        User user = userService.findById(userId);

        if (user == null) {
            logger.warn("세션에 저장된 ID({})에 해당하는 사용자가 없음", userId);
            SessionUtil.invalidateSession(request);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("unauthorized", null)
            );
        }

        logger.info("현재 로그인된 사용자 정보 조회: {}", user.getEmail());

        // 클라이언트에 반환할 사용자 정보에서 민감한 정보 제거
        User userResponse = sanitizeUserForResponse(user);

        ApiResponse<User> response = new ApiResponse<>("success", userResponse);
        return ResponseEntity.ok(response);
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