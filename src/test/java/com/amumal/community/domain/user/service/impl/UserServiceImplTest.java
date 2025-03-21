package com.amumal.community.domain.user.service.impl;

import com.amumal.community.domain.user.dto.request.UserUpdateRequest;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.repository.UserRepository;
import com.amumal.community.global.s3.service.S3Service;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3Service s3Service;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("previousNickname")
                .profileImage("http://s3.amazon.com/bucket/oldImage.jpg")
                .password("previousPassword")
                .build();
    }


    @Test
    @DisplayName("기존 이미지 삭제 후 새로운 이미지 업로드하여 프로필 업데이트한다")
    void updateProfile_Success_WithImagefile() throws IOException {
        //Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUserId(testUser.getId());
        request.setNickname("newNickname");
        MultipartFile newImage = new MockMultipartFile("profileImage", "newImage.png", "image/png", "new image content".getBytes());

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(s3Service.isValidS3Url(testUser.getProfileImage())).thenReturn(true);
        doNothing().when(s3Service).deleteImage("http://s3.amazon.com/bucket/oldImage.jpg");
        when(s3Service.uploadImage(newImage)).thenReturn("http://s3.amazon.com/bucket/newImage.jpg");

        //When
        userService.updateProfile(request, newImage);

        //Then
        assertEquals("newNickname", testUser.getNickname());
        assertEquals("http://s3.amazon.com/bucket/newImage.jpg", testUser.getProfileImage());
        verify(s3Service).deleteImage("http://s3.amazon.com/bucket/oldImage.jpg");
        verify(s3Service).uploadImage(newImage);
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("프로필 이미지 없이 닉네임만 업데이트하여 프로필 업데이트 한다")
    void updateProfile_Success_WithoutImage() throws IOException {
        // Given: 프로필 이미지가 null 또는 empty인 경우
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUserId(testUser.getId());
        request.setNickname("updatedNickname");
        MultipartFile emptyImage = new MockMultipartFile("profileImage", "empty.png", "image/png", new byte[0]);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        // 이미지 업로드는 호출되지 않아야 하므로 isValidS3Url 및 deleteImage는 호출되지 않음

        // When
        userService.updateProfile(request, emptyImage);

        // Then: 닉네임 업데이트만 적용되고, 기존 프로필 이미지는 그대로 유지됨
        assertEquals("updatedNickname", testUser.getNickname());
        assertEquals("http://s3.amazon.com/bucket/oldImage.jpg", testUser.getProfileImage());
        verify(s3Service, never()).uploadImage(any(MultipartFile.class));
        verify(s3Service, never()).deleteImage(anyString());
    }

    @Test
    @DisplayName("중복된 닉네임으로 변경하여 프로필 업데이트 실패한다")
    void updateProfile_Fail_WithDuplicatedNickname() throws IOException {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUserId(testUser.getId());
        request.setNickname("duplicateNickname");
        MultipartFile dummyImage = new MockMultipartFile(
                "profileImage", "file.png", "image/png", "dummy".getBytes()
        );

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByNickname("duplicateNickname")).thenReturn(true);

        // When & Then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateProfile(request, dummyImage)
        );
        assertEquals("이미 존재하는 닉네임입니다.", ex.getMessage());

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).existsByNickname("duplicateNickname");
        verify(s3Service, never()).deleteImage(anyString());
        verify(s3Service, never()).uploadImage(any());
    }
    @Test
    void updatePassword() {
    }

    @Test
    void getUserInfo() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void findById() {
    }

    public User getTestUser() {
        return testUser;
    }

    public void setTestUser(User testUser) {
        this.testUser = testUser;
    }
}