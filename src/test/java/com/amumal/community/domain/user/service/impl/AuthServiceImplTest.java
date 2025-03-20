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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

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

    // 공통 테스트 데이터
    private final String email = "email@email.com";
    private final String rawPassword = "password1";
    private final String encodedPassword = "encodedPassword1";
    private final String nickname = "tester";
    private final String imageUrl = "http://s3.amazon.com/bucket/image.jpg";
    private final Long userId = 1L;
    private final String accessToken = "accessToken";
    private final String refreshToken = "refreshToken";

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    // 실제 테스트에서는 MockMultipartFile을 사용하여 실제 동작하는 객체를 사용
    private MultipartFile profileImage;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest(email, rawPassword, nickname);
        loginRequest = new LoginRequest(email, rawPassword);
        // 실제 동작하는 객체(MockMultipartFile)는 내부에서 isEmpty() 등 기본 동작을 수행합니다.
        profileImage = new MockMultipartFile("profileImage", "test.png", "image/png", "dummy image content".getBytes());
    }


    @Test
    @DisplayName("회원가입 성공 - 프로필 이미지가 있는 경우")
    void signup_Success_WithImage() throws IOException {
        // Given
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByNickname(nickname)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        // profileImage는 MockMultipartFile이므로 기본적으로 isEmpty()는 false를 반환
        // 실제로 S3Service를 목으로 설정
        when(s3Service.uploadImage(profileImage)).thenReturn(imageUrl);
        User savedUser = User.builder()
                .id(userId)
                .email(email)
                .nickname(nickname)
                .password(encodedPassword)
                .profileImage(imageUrl)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        Long resultID = authService.signup(signupRequest, profileImage);

        // Then
        assertEquals(userId, resultID);
        verify(userRepository).existsByEmail(email);
        verify(userRepository).existsByNickname(nickname);
        verify(passwordEncoder).encode(rawPassword);
        verify(s3Service).uploadImage(profileImage);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 성공 - 프로필 이미지가 없는 경우")
    void signup_Success_WithoutImage() throws IOException {
        // Given: 프로필 이미지가 비어있는 경우, 즉 null 혹은 isEmpty()가 true인 경우
        MultipartFile emptyImage = new MockMultipartFile("profileImage", "empty.png", "image/png", new byte[0]);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByNickname(nickname)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        // emptyImage.isEmpty()는 true이므로 S3 업로드 호출되지 않음.
        User savedUser = User.builder()
                .id(userId)
                .email(email)
                .nickname(nickname)
                .password(encodedPassword)
                .profileImage(null)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        Long resultID = authService.signup(signupRequest, emptyImage);

        // Then
        assertEquals(userId, resultID);
        verify(userRepository).existsByEmail(email);
        verify(userRepository).existsByNickname(nickname);
        verify(passwordEncoder).encode(rawPassword);
        verify(s3Service, never()).uploadImage(any(MultipartFile.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signup_Failure_EmailExists() {
        // Given
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.signup(signupRequest, profileImage));
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).existsByNickname(anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 닉네임")
    void signup_Failure_NicknameExists() {
        // Given
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByNickname(nickname)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.signup(signupRequest, profileImage));
        assertEquals("이미 존재하는 닉네임입니다.", exception.getMessage());
        verify(userRepository).existsByEmail(email);
        verify(userRepository).existsByNickname(nickname);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미지 업로드 중 IOException 발생")
    void signup_Failure_ImageUploadIOException() throws IOException {
        // Given
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByNickname(nickname)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        // 이미지 업로드 시 IOException 발생하도록 설정
        when(s3Service.uploadImage(profileImage)).thenThrow(new IOException("Upload failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.signup(signupRequest, profileImage));
        assertTrue(exception.getMessage().contains("이미지 업로드 중 오류 발생"));
        verify(s3Service).uploadImage(profileImage);
    }



    @Test
    @DisplayName("로그인 성공 케이스")
    void testLogin_Success() {
        // Given
        User user = User.builder()
                .id(userId)
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .profileImage(imageUrl)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(userId, email)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(userId, email)).thenReturn(refreshToken);

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(email, response.getEmail());
        assertEquals(nickname, response.getNickname());
        assertEquals(imageUrl, response.getProfileImage());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 값이 없을 경우")
    void login_Fail_NoEmail() {
        // Given: 이메일이 null인 로그인 요청
        LoginRequest invalidLoginRequest = new LoginRequest(null, rawPassword);
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authService.login(invalidLoginRequest));

        // 그리고 빈 문자열("")인 경우도 테스트
        LoginRequest emptyEmailRequest = new LoginRequest("", rawPassword);
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> authService.login(emptyEmailRequest));

        verify(userRepository, times(1)).findByEmail(null);
        verify(userRepository, times(1)).findByEmail("");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 값이 없을 경우")
    void login_Fail_NoPassword() {
        // Given: 비밀번호가 null인 로그인 요청
        LoginRequest invalidLoginRequest = new LoginRequest(email, null);
        User user = User.builder()
                .id(userId)
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .profileImage(imageUrl)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // 비밀번호가 null인 경우, passwordEncoder.matches(null, encodedPassword)가 false를 반환하도록 설정
        when(passwordEncoder.matches(null, encodedPassword)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authService.login(invalidLoginRequest));
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(null, encodedPassword);
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Fail_WrongPassword() {
        // Given
        LoginRequest wrongPasswordRequest = new LoginRequest(email, "wrongPassword");
        User user = User.builder()
                .id(userId)
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .profileImage(imageUrl)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", encodedPassword)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authService.login(wrongPasswordRequest));
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches("wrongPassword", encodedPassword);
    }



    @Test
    @DisplayName("리프레시 토큰 갱신 성공 케이스")
    void testRefreshToken_Success() {
        // Given: refreshToken으로부터 이메일과 userId 추출
        when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
        when(jwtUtil.extractUserId(refreshToken)).thenReturn(userId);
        User user = User.builder()
                .id(userId)
                .email(email)
                .nickname(nickname)
                .profileImage(imageUrl)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken(refreshToken, email)).thenReturn(true);
        when(jwtUtil.generateToken(userId, email)).thenReturn(accessToken);

        // When
        AuthResponse response = authService.refreshToken(refreshToken);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(email, response.getEmail());
        assertEquals(nickname, response.getNickname());
        assertEquals(imageUrl, response.getProfileImage());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(jwtUtil).extractUsername(refreshToken);
        verify(jwtUtil).extractUserId(refreshToken);
        verify(jwtUtil).validateToken(refreshToken, email);
    }

    @Test
    @DisplayName("리프레시 토큰 갱신 실패 - 토큰 유효성 실패")
    void testRefreshToken_Failure_InvalidToken() {
        // Given
        when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
        when(jwtUtil.extractUserId(refreshToken)).thenReturn(userId);
        User user = User.builder()
                .id(userId)
                .email(email)
                .nickname(nickname)
                .profileImage(imageUrl)
                .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // 유효하지 않은 토큰으로 판단
        when(jwtUtil.validateToken(refreshToken, email)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.refreshToken(refreshToken));
        assertEquals("만료되거나 유효하지 않은 리프레시 토큰입니다.", exception.getMessage());
        verify(jwtUtil).validateToken(refreshToken, email);
    }
}
