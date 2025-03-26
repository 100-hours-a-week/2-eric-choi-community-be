package com.amumal.community.domain.user.service.impl;

import com.amumal.community.domain.user.dto.request.PasswordUpdateRequest;
import com.amumal.community.domain.user.dto.request.UserUpdateRequest;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.repository.UserRepository;
import com.amumal.community.global.s3.service.S3Service;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    // 공통 테스트 데이터
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@example.com";
    private static final String OLD_NICKNAME = "previousNickname";
    private static final String NEW_NICKNAME = "newNickname";
    private static final String OLD_IMAGE_URL = "http://s3.amazon.com/bucket/oldImage.jpg";
    private static final String NEW_IMAGE_URL = "http://s3.amazon.com/bucket/newImage.jpg";
    private static final String OLD_PASSWORD = "previousPassword";
    private static final String NEW_PASSWORD = "newPassword123";
    private static final String NEW_ENCODED_PASSWORD = "encodedNewPassword";
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private S3Service s3Service;
    private User testUser;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 기본 테스트 유저 생성
        testUser = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .nickname(OLD_NICKNAME)
                .profileImage(OLD_IMAGE_URL)
                .password(OLD_PASSWORD)
                .build();

        // 기본 업데이트 요청 객체 생성
        updateRequest = new UserUpdateRequest();
        updateRequest.setUserId(USER_ID);
        updateRequest.setNickname(NEW_NICKNAME);

        // 기본 레포지토리 설정
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
    }

    @Nested
    @DisplayName("프로필 업데이트 테스트")
    class ProfileUpdateTest {

        @Test
        @DisplayName("이미지 변경 포함 프로필 업데이트 성공")
        void updateProfile_WithNewImage_Success() throws IOException {
            // Given
            MultipartFile newImage = new MockMultipartFile("profileImage", "newImage.png",
                    "image/png", "new image content".getBytes());

            when(s3Service.isValidS3Url(OLD_IMAGE_URL)).thenReturn(true);
            doNothing().when(s3Service).deleteImage(OLD_IMAGE_URL);
            when(s3Service.uploadImage(newImage)).thenReturn(NEW_IMAGE_URL);

            // When
            userService.updateProfile(updateRequest, newImage);

            // Then
            assertEquals(NEW_NICKNAME, testUser.getNickname());
            assertEquals(NEW_IMAGE_URL, testUser.getProfileImage());

            // 적절한 메서드 호출 확인
            verify(s3Service).deleteImage(OLD_IMAGE_URL);
            verify(s3Service).uploadImage(newImage);
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("이미지 변경 없이 닉네임만 업데이트 성공")
        void updateProfile_WithoutImage_Success() throws IOException {
            // Given
            MultipartFile emptyImage = new MockMultipartFile("profileImage", "empty.png",
                    "image/png", new byte[0]);

            // When
            userService.updateProfile(updateRequest, emptyImage);

            // Then
            assertEquals(NEW_NICKNAME, testUser.getNickname());
            assertEquals(OLD_IMAGE_URL, testUser.getProfileImage()); // 이미지는 변경 없음

            // 이미지 처리 메서드 호출 안됨 확인
            verify(s3Service, never()).uploadImage(any(MultipartFile.class));
            verify(s3Service, never()).deleteImage(anyString());
        }

        @Test
        @DisplayName("중복 닉네임으로 업데이트 시도 시 실패")
        void updateProfile_WithDuplicateNickname_Fails() throws IOException {
            // Given
            MultipartFile dummyImage = new MockMultipartFile("profileImage", "file.png",
                    "image/png", "dummy".getBytes());

            when(userRepository.existsByNickname(NEW_NICKNAME)).thenReturn(true);

            // When & Then
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.updateProfile(updateRequest, dummyImage));

            assertEquals("이미 존재하는 닉네임입니다.", ex.getMessage());

            // 이미지 처리 메서드 호출 안됨 확인
            verify(s3Service, never()).deleteImage(anyString());
            verify(s3Service, never()).uploadImage(any());
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class PasswordUpdateTest {

        @Test
        @DisplayName("비밀번호 변경 성공")
        void updatePassword_Success() {
            // Given
            PasswordUpdateRequest request = new PasswordUpdateRequest();
            request.setUserId(USER_ID);
            request.setPassword(NEW_PASSWORD);
            request.setConfirmPassword(NEW_PASSWORD);

            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_ENCODED_PASSWORD);

            // When
            userService.updatePassword(request);

            // Then
            assertEquals(NEW_ENCODED_PASSWORD, testUser.getPassword());
            verify(passwordEncoder).encode(NEW_PASSWORD);
        }

        @Test
        @DisplayName("비밀번호와 확인 비밀번호 불일치 시 실패")
        void updatePassword_PasswordMismatch_Fails() {
            // Given
            PasswordUpdateRequest request = new PasswordUpdateRequest();
            request.setUserId(USER_ID);
            request.setPassword(NEW_PASSWORD);
            request.setConfirmPassword("differentPassword");

            // When & Then
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.updatePassword(request));
            assertEquals("비밀번호와 비밀번호 확인이 일치하지 않습니다.", ex.getMessage());

            // 비밀번호 인코딩 호출 안됨 확인
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class UserQueryTest {

        @Test
        @DisplayName("사용자 정보 조회 성공")
        void getUserInfo_Success() {
            // When
            UserInfoResponse response = userService.getUserInfo(USER_ID);

            // Then
            assertNotNull(response);
            assertEquals(USER_ID, response.getId());
            assertEquals(USER_EMAIL, response.getEmail());
            assertEquals(OLD_NICKNAME, response.getNickname());
            assertEquals(OLD_IMAGE_URL, response.getProfileImage());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void getUserInfo_UserNotFound_ThrowsException() {
            // Given
            Long nonExistingUserId = 999L;
            when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> userService.getUserInfo(nonExistingUserId));
        }
    }

    @Nested
    @DisplayName("사용자 삭제 테스트")
    class UserDeleteTest {

        @Test
        @DisplayName("사용자 삭제 성공")
        void deleteUser_Success() {
            // When
            userService.deleteUser(USER_ID);

            // Then
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 삭제 시 예외 발생")
        void deleteUser_UserNotFound_ThrowsException() {
            // Given
            Long nonExistingUserId = 999L;
            when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> userService.deleteUser(nonExistingUserId));
        }
    }
}