package com.amumal.community.domain.user.service.impl;

import com.amumal.community.domain.user.dto.request.LoginRequest;
import com.amumal.community.domain.user.dto.request.SignupRequest;
import com.amumal.community.domain.user.dto.response.AuthResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.repository.UserRepository;
import com.amumal.community.global.s3.service.S3Service;
import com.amumal.community.global.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    // 테스트 상수 정의
    private static final String EMAIL = "email@email.com";
    private static final String RAW_PASSWORD = "password1";
    private static final String ENCODED_PASSWORD = "encodedPassword1";
    private static final String NICKNAME = "tester";
    private static final String IMAGE_URL = "http://s3.amazon.com/bucket/image.jpg";
    private static final Long USER_ID = 1L;
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private S3Service s3Service;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTest {
        private SignupRequest signupRequest;
        private MultipartFile profileImage;
        private User savedUser;

        @BeforeEach
        void setUp() {
            // 회원가입 요청 설정
            signupRequest = new SignupRequest(EMAIL, RAW_PASSWORD, NICKNAME);

            // 프로필 이미지 설정
            profileImage = new MockMultipartFile("profileImage", "test.png",
                    "image/png", "dummy image content".getBytes());

            // 저장된 사용자 설정
            savedUser = User.builder()
                    .id(USER_ID)
                    .email(EMAIL)
                    .nickname(NICKNAME)
                    .password(ENCODED_PASSWORD)
                    .profileImage(IMAGE_URL)
                    .build();

            // 기본 모킹 설정
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        }

        @Test
        @DisplayName("프로필 이미지가 있는 경우 회원가입 성공")
        void signup_WithImage_Success() throws IOException {
            // Given
            when(s3Service.uploadImage(profileImage)).thenReturn(IMAGE_URL);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // When
            Long resultID = authService.signup(signupRequest, profileImage);

            // Then
            assertEquals(USER_ID, resultID);
            verify(s3Service).uploadImage(profileImage);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("프로필 이미지가 없는 경우 회원가입 성공")
        void signup_WithoutImage_Success() throws IOException {
            // Given: 빈 이미지 설정
            MultipartFile emptyImage = new MockMultipartFile("profileImage", "empty.png",
                    "image/png", new byte[0]);

            User userWithoutImage = User.builder()
                    .id(USER_ID)
                    .email(EMAIL)
                    .nickname(NICKNAME)
                    .password(ENCODED_PASSWORD)
                    .profileImage(null)
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(userWithoutImage);

            // When
            Long resultID = authService.signup(signupRequest, emptyImage);

            // Then
            assertEquals(USER_ID, resultID);
            verify(s3Service, never()).uploadImage(any(MultipartFile.class));
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입 실패")
        void signup_EmailExists_Fails() {
            // Given
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.signup(signupRequest, profileImage));

            assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
            verify(userRepository).existsByEmail(EMAIL);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 존재하는 닉네임으로 회원가입 실패")
        void signup_NicknameExists_Fails() {
            // Given
            when(userRepository.existsByNickname(NICKNAME)).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.signup(signupRequest, profileImage));

            assertEquals("이미 존재하는 닉네임입니다.", exception.getMessage());
            verify(userRepository).existsByNickname(NICKNAME);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미지 업로드 중 IO 예외 발생 시 실패")
        void signup_ImageUploadIOException_Fails() throws IOException {
            // Given
            when(s3Service.uploadImage(profileImage)).thenThrow(new IOException("Upload failed"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.signup(signupRequest, profileImage));

            assertTrue(exception.getMessage().contains("이미지 업로드 중 오류 발생"));
            verify(s3Service).uploadImage(profileImage);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        private LoginRequest loginRequest;
        private User user;

        @BeforeEach
        void setUp() {
            // 로그인 요청 설정
            loginRequest = new LoginRequest(EMAIL, RAW_PASSWORD);

            // 사용자 설정
            user = User.builder()
                    .id(USER_ID)
                    .email(EMAIL)
                    .password(ENCODED_PASSWORD)
                    .nickname(NICKNAME)
                    .profileImage(IMAGE_URL)
                    .build();

        }

        @Test
        @DisplayName("로그인 성공")
        void login_Success() {
            // Given
            // 스텁을 명시적으로 설정
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(jwtUtil.generateToken(USER_ID, EMAIL)).thenReturn(ACCESS_TOKEN);
            when(jwtUtil.generateRefreshToken(USER_ID, EMAIL)).thenReturn(REFRESH_TOKEN);

            // When
            AuthResponse response = authService.login(loginRequest);

            // Then
            assertNotNull(response);
            assertEquals(USER_ID, response.getUserId());
            assertEquals(EMAIL, response.getEmail());
            assertEquals(NICKNAME, response.getNickname());
            assertEquals(IMAGE_URL, response.getProfileImage());
            assertEquals(ACCESS_TOKEN, response.getAccessToken());
            assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        }

        @Test
        @DisplayName("이메일 미입력 시 로그인 실패")
        void login_NoEmail_Fails() {
            // Given
            LoginRequest invalidRequest = new LoginRequest(null, RAW_PASSWORD);
            when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> authService.login(invalidRequest));
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void login_WrongPassword_Fails() {
            // Given
            String wrongPassword = "wrongPassword";
            LoginRequest invalidRequest = new LoginRequest(EMAIL, wrongPassword);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> authService.login(invalidRequest));
            verify(jwtUtil, never()).generateToken(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("토큰 관련 테스트")
    class TokenTest {

        private User user;

        @BeforeEach
        void setUp() {
            // 사용자 설정
            user = User.builder()
                    .id(USER_ID)
                    .email(EMAIL)
                    .nickname(NICKNAME)
                    .profileImage(IMAGE_URL)
                    .build();

            // 기본 모킹 설정
            when(jwtUtil.extractUsername(REFRESH_TOKEN)).thenReturn(EMAIL);
            when(jwtUtil.extractUserId(REFRESH_TOKEN)).thenReturn(USER_ID);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        }

        @Test
        @DisplayName("토큰 갱신 성공")
        void refreshToken_Success() {
            // Given
            when(jwtUtil.validateToken(REFRESH_TOKEN, EMAIL)).thenReturn(true);
            when(jwtUtil.generateToken(USER_ID, EMAIL)).thenReturn(ACCESS_TOKEN);

            // When
            AuthResponse response = authService.refreshToken(REFRESH_TOKEN);

            // Then
            assertNotNull(response);
            assertEquals(USER_ID, response.getUserId());
            assertEquals(EMAIL, response.getEmail());
            assertEquals(NICKNAME, response.getNickname());
            assertEquals(IMAGE_URL, response.getProfileImage());
            assertEquals(ACCESS_TOKEN, response.getAccessToken());
            assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 갱신 실패")
        void refreshToken_InvalidToken_Fails() {
            // Given
            when(jwtUtil.validateToken(REFRESH_TOKEN, EMAIL)).thenReturn(false);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.refreshToken(REFRESH_TOKEN));

            assertEquals("만료되거나 유효하지 않은 리프레시 토큰입니다.", exception.getMessage());
            verify(jwtUtil, never()).generateToken(anyLong(), anyString());
        }
    }
}