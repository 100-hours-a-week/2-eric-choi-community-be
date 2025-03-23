package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.dto.response.AuthResponse;
import com.amumal.community.domain.user.service.AuthService;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.config.filter.JwtAuthenticationFilter;
import com.amumal.community.global.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    // 추가된 부분 - JwtUtil과 JwtAuthenticationFilter를 모킹합니다
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 이하 테스트 코드는 동일하게 유지

    // -------------------- 성공 케이스 --------------------

    @Test
    @DisplayName("로그인 성공 시 올바른 응답을 반환한다")
    void login_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
        AuthResponse authResponse = AuthResponse.builder()
                .userId(1L)
                .email("user@example.com")
                .nickname("tester")
                .profileImage("http://example.com/profile.jpg")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.status").value("login_success"))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.profileImage").value("http://example.com/profile.jpg"))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("회원가입 성공 시 CREATED 응답을 반환한다")
    void register_Success() throws Exception {
        SignupRequest signupRequest = new SignupRequest("newuser@example.com", "password", "newtester");
        String userInfoJson = objectMapper.writeValueAsString(signupRequest);
        MockMultipartFile userInfoPart = new MockMultipartFile("userInfo", "", "application/json", userInfoJson.getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("profileImage", "image.png", "image/png", "dummy".getBytes());

        when(authService.signup(any(SignupRequest.class), any())).thenReturn(1L);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/new")
                        .file(userInfoPart)
                        .file(imagePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("register_success"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("리프레시 토큰 성공 시 새로운 accessToken을 반환하고 쿠키에 refreshToken을 설정한다")
    void refreshToken_Success() throws Exception {
        String refreshToken = "refreshToken";
        AuthResponse authResponse = AuthResponse.builder()
                .userId(1L)
                .email("user@example.com")
                .nickname("tester")
                .profileImage("http://example.com/profile.jpg")
                .accessToken("newAccessToken")
                .refreshToken(refreshToken)
                .build();

        when(authService.refreshToken(refreshToken)).thenReturn(authResponse);
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refresh_token")))
                .andExpect(jsonPath("$.status").value("token_refresh_success"))
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("로그아웃 시 쿠키 삭제 후 성공 응답을 반환한다")
    void logout_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
                .andExpect(jsonPath("$.status").value("logout_success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("로그인 실패: 잘못된 로그인 정보로 인해 예외가 발생한다")
    void login_Failure_InvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "wrongPassword");

        // 로그인 실패 시 authService.login()이 예외를 던지도록 설정
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("이메일 또는 비밀번호 오류"));

        mockMvc.perform(MockMvcRequestBuilders.post("/users/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("이메일 또는 비밀번호 오류"));
    }

    @Test
    @DisplayName("회원가입 실패: 이미 존재하는 이메일 혹은 닉네임으로 인해 예외가 발생한다")
    void register_Failure_Duplicate() throws Exception {
        SignupRequest signupRequest = new SignupRequest("existing@example.com", "password", "existingUser");
        String userInfoJson = objectMapper.writeValueAsString(signupRequest);
        MockMultipartFile userInfoPart = new MockMultipartFile("userInfo", "", "application/json", userInfoJson.getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("profileImage", "image.png", "image/png", "dummy".getBytes());

        // 회원가입 시 이미 중복된 정보로 인한 예외 발생을 가정
        when(authService.signup(any(SignupRequest.class), any()))
                .thenThrow(new IllegalArgumentException("이미 존재하는 이메일입니다."));

        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/new")
                        .file(userInfoPart)
                        .file(imagePart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("이미 존재하는 이메일입니다."));
    }

    @Test
    @DisplayName("리프레시 토큰 실패: 쿠키에 리프레시 토큰이 없으면 UNAUTHORIZED 응답을 반환한다")
    void refreshToken_Failure_NoCookie() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("invalid_refresh_token"));
    }

    @Test
    @DisplayName("리프레시 토큰 실패: authService.refreshToken 호출 시 예외가 발생하면 UNAUTHORIZED 응답을 반환한다")
    void refreshToken_Failure_ServiceException() throws Exception {
        String refreshToken = "invalidRefreshToken";
        when(authService.refreshToken(refreshToken))
                .thenThrow(new IllegalArgumentException("만료되거나 유효하지 않은 리프레시 토큰입니다."));
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("token_refresh_failed"));
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 실패: 인증되지 않은 사용자는 UNAUTHORIZED 응답을 반환한다")
    void getCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("unauthorized"));
    }
}