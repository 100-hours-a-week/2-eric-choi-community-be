package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.TestSecurityConfig;
import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.dto.response.AuthResponse;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.global.config.security.JwtUserDetails;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.amumal.community.domain.user.service.AuthService authService;

    @Autowired
    private com.amumal.community.domain.user.service.UserService userService;

    // ------------------ 로그인 테스트 ------------------

    @Test
    @DisplayName("로그인 성공 케이스")
    public void loginSuccessTest() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setRefreshToken("dummyRefreshToken");
        // 컨트롤러에서는 응답 전 refreshToken을 null로 설정함

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // when & then
        mockMvc.perform(post("/users/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("login_success"))
                // 응답 데이터에 refreshToken은 포함되지 않아야 함
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("로그인 실패 케이스 - 잘못된 비밀번호로 로그인")
    public void loginFailureTest() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new CustomException(CustomResponseStatus.UNAUTHORIZED, "Invalid credentials"));

        // when & then: 401 Unauthorized를 기대해야 함
        mockMvc.perform(post("/users/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));
    }


    // ------------------ 회원가입 테스트 ------------------

    @Test
    @DisplayName("회원가입 성공 케이스")
    public void registerSuccessTest() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password");
        signupRequest.setNickname("newuser");

        when(authService.signup(any(SignupRequest.class), any()))
                .thenReturn(1L);

        MockMultipartFile userInfoPart = new MockMultipartFile(
                "userInfo", "", "application/json",
                objectMapper.writeValueAsBytes(signupRequest));
        // 프로필 이미지 파일은 선택 사항
        MockMultipartFile filePart = new MockMultipartFile(
                "profileImage", "profile.png", MediaType.IMAGE_PNG_VALUE,
                "dummyImageContent".getBytes());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/new")
                        .file(userInfoPart)
                        .file(filePart)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("register_success"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("회원가입 실패 케이스 - 예외 발생")
    public void registerFailureTest() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password");
        signupRequest.setNickname("newuser");

        when(authService.signup(any(SignupRequest.class), any()))
                .thenThrow(new RuntimeException("Registration failed"));

        MockMultipartFile userInfoPart = new MockMultipartFile(
                "userInfo", "", "application/json",
                objectMapper.writeValueAsBytes(signupRequest));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/new")
                        .file(userInfoPart)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    // ------------------ 토큰 재발급 테스트 ------------------

    @Test
    @DisplayName("토큰 재발급 성공 케이스")
    public void refreshTokenSuccessTest() throws Exception {
        // given
        AuthResponse authResponse = new AuthResponse();
        authResponse.setRefreshToken("newDummyRefreshToken");

        when(authService.refreshToken(any(String.class))).thenReturn(authResponse);

        // when & then
        mockMvc.perform(post("/users/refresh")
                        .cookie(new Cookie("refresh_token", "dummyRefreshToken"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("token_refresh_success"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("토큰 재발급 실패 케이스 - 쿠키 없음")
    public void refreshTokenMissingCookieTest() throws Exception {
        mockMvc.perform(post("/users/refresh")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid_refresh_token"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 케이스 - 예외 발생")
    public void refreshTokenExceptionTest() throws Exception {
        when(authService.refreshToken(any(String.class)))
                .thenThrow(new RuntimeException("Token expired"));

        mockMvc.perform(post("/users/refresh")
                        .cookie(new Cookie("refresh_token", "dummyRefreshToken"))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("token_refresh_failed"));
    }

    // ------------------ 로그아웃 테스트 ------------------

    @Test
    @DisplayName("로그아웃 테스트")
    public void logoutTest() throws Exception {
        mockMvc.perform(post("/users/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("logout_success"))
                // 쿠키의 Max-Age가 0으로 설정되어 삭제되었음을 확인
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    // ------------------ 현재 사용자 정보 조회 테스트 ------------------

    @Test
    @DisplayName("현재 사용자 정보 조회 성공 케이스")
    public void getCurrentUserSuccessTest() throws Exception {
        // 인증된 JwtUserDetails 모의 객체 생성
        JwtUserDetails dummyUserDetails = Mockito.mock(JwtUserDetails.class);
        when(dummyUserDetails.getId()).thenReturn(1L);
        when(dummyUserDetails.getUsername()).thenReturn("user@example.com");

        // User 엔티티는 빌더를 사용하여 생성
        User dummyUserEntity = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("TestUser")
                .password("encodedPassword")
                .profileImage("dummyProfileImage")
                .build();
        when(userService.findById(1L)).thenReturn(dummyUserEntity);

        // authService에서 User 엔티티를 UserInfoResponse로 변환하도록 모의
        UserInfoResponse userInfoResponse = new UserInfoResponse();
        userInfoResponse.setEmail(dummyUserEntity.getEmail());
        userInfoResponse.setNickname(dummyUserEntity.getNickname());
        userInfoResponse.setProfileImage(dummyUserEntity.getProfileImage());
        when(authService.convertToUserResponse(dummyUserEntity)).thenReturn(userInfoResponse);

        // when & then
        mockMvc.perform(get("/users/me")
                        .with(user(dummyUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("TestUser"))
                .andExpect(jsonPath("$.data.profileImage").value("dummyProfileImage"));
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 실패 케이스 - 미인증")
    public void getCurrentUserUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public com.amumal.community.domain.user.service.AuthService authService() {
            return Mockito.mock(com.amumal.community.domain.user.service.AuthService.class);
        }

        @Bean
        public com.amumal.community.domain.user.service.UserService userService() {
            return Mockito.mock(com.amumal.community.domain.user.service.UserService.class);
        }
    }
}
