package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.TestSecurityConfig;
import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.dto.response.AuthResponse;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.AuthService;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.config.security.JwtUserDetails;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({TestSecurityConfig.class, AuthControllerTest.MockConfig.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {AuthController.class, TestSecurityConfig.class, AuthControllerTest.MockConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    // 테스트 상수
    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "password";
    private static final String NICKNAME = "TestUser";
    private static final String PROFILE_IMAGE = "dummyProfileImage";
    private static final String REFRESH_TOKEN = "dummyRefreshToken";
    private static final String NEW_REFRESH_TOKEN = "newDummyRefreshToken";
    private static final String WRONG_PASSWORD = "wrongpassword";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public AuthService authService() {
            return mock(AuthService.class);
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void login_validCredentials_success() throws Exception {
            // Given
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(EMAIL);
            loginRequest.setPassword(PASSWORD);

            AuthResponse authResponse = new AuthResponse();
            authResponse.setRefreshToken(REFRESH_TOKEN);

            when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/users/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("login_success"))
                    .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE));
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 비밀번호")
        void login_invalidCredentials_fails() throws Exception {
            // Given
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(EMAIL);
            loginRequest.setPassword(WRONG_PASSWORD);

            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new CustomException(CustomResponseStatus.UNAUTHORIZED, "Invalid credentials"));

            // When & Then
            mockMvc.perform(post("/users/auth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("unauthorized"));
        }
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTest {

        @Test
        @DisplayName("회원가입 성공")
        void signup_validRequest_success() throws Exception {
            // Given
            SignupRequest signupRequest = new SignupRequest();
            signupRequest.setEmail("newuser@example.com");
            signupRequest.setPassword(PASSWORD);
            signupRequest.setNickname("newuser");

            when(authService.signup(any(SignupRequest.class), any()))
                    .thenReturn(USER_ID);

            MockMultipartFile userInfoPart = new MockMultipartFile(
                    "userInfo", "", "application/json",
                    objectMapper.writeValueAsBytes(signupRequest));

            MockMultipartFile filePart = new MockMultipartFile(
                    "profileImage", "profile.png", MediaType.IMAGE_PNG_VALUE,
                    "dummyImageContent".getBytes());

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.multipart("/users/new")
                            .file(userInfoPart)
                            .file(filePart)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("register_success"))
                    .andExpect(jsonPath("$.data").value(USER_ID));
        }

        @Test
        @DisplayName("회원가입 실패 - 예외 발생")
        void signup_exception_fails() throws Exception {
            // Given
            SignupRequest signupRequest = new SignupRequest();
            signupRequest.setEmail("newuser@example.com");
            signupRequest.setPassword(PASSWORD);
            signupRequest.setNickname("newuser");

            when(authService.signup(any(SignupRequest.class), any()))
                    .thenThrow(new RuntimeException("Registration failed"));

            MockMultipartFile userInfoPart = new MockMultipartFile(
                    "userInfo", "", "application/json",
                    objectMapper.writeValueAsBytes(signupRequest));

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.multipart("/users/new")
                            .file(userInfoPart)
                            .with(csrf()))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("토큰 관련 테스트")
    class TokenTest {

        @Test
        @DisplayName("토큰 재발급 성공")
        void refreshToken_validToken_success() throws Exception {
            // Given
            AuthResponse authResponse = new AuthResponse();
            authResponse.setRefreshToken(NEW_REFRESH_TOKEN);

            when(authService.refreshToken(any(String.class))).thenReturn(authResponse);

            // When & Then
            mockMvc.perform(post("/users/refresh")
                            .cookie(new Cookie("refresh_token", REFRESH_TOKEN))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("token_refresh_success"))
                    .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE));
        }

        @Test
        @DisplayName("토큰 재발급 실패 - 쿠키 없음")
        void refreshToken_noCookie_fails() throws Exception {
            // When & Then
            mockMvc.perform(post("/users/refresh")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("invalid_refresh_token"));
        }

        @Test
        @DisplayName("토큰 재발급 실패 - 토큰 만료")
        void refreshToken_expired_fails() throws Exception {
            // Given
            when(authService.refreshToken(any(String.class)))
                    .thenThrow(new RuntimeException("Token expired"));

            // When & Then
            mockMvc.perform(post("/users/refresh")
                            .cookie(new Cookie("refresh_token", REFRESH_TOKEN))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("token_refresh_failed"));
        }

        @Test
        @DisplayName("로그아웃 성공")
        void logout_success() throws Exception {
            // When & Then
            mockMvc.perform(post("/users/logout")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("logout_success"))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
        }
    }

    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    class UserInfoTest {

        @Test
        @DisplayName("현재 사용자 정보 조회 성공")
        void getCurrentUser_authenticated_success() throws Exception {
            // Given
            JwtUserDetails dummyUserDetails = mock(JwtUserDetails.class);
            when(dummyUserDetails.getId()).thenReturn(USER_ID);
            when(dummyUserDetails.getUsername()).thenReturn(EMAIL);

            User dummyUserEntity = User.builder()
                    .id(USER_ID)
                    .email(EMAIL)
                    .nickname(NICKNAME)
                    .password("encodedPassword")
                    .profileImage(PROFILE_IMAGE)
                    .build();

            when(userService.findById(USER_ID)).thenReturn(dummyUserEntity);

            UserInfoResponse userInfoResponse = new UserInfoResponse();
            userInfoResponse.setEmail(EMAIL);
            userInfoResponse.setNickname(NICKNAME);
            userInfoResponse.setProfileImage(PROFILE_IMAGE);

            when(authService.convertToUserResponse(dummyUserEntity)).thenReturn(userInfoResponse);

            // When & Then
            mockMvc.perform(get("/users/me")
                            .with(user(dummyUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.email").value(EMAIL))
                    .andExpect(jsonPath("$.data.nickname").value(NICKNAME))
                    .andExpect(jsonPath("$.data.profileImage").value(PROFILE_IMAGE));
        }

        @Test
        @DisplayName("현재 사용자 정보 조회 실패 - 미인증")
        void getCurrentUser_unauthenticated_fails() throws Exception {
            // When & Then (인증 정보 없이 요청)
            mockMvc.perform(get("/users/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("unauthorized"));
        }
    }
}