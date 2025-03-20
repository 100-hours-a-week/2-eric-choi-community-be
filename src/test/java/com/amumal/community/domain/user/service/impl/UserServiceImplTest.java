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
import static org.mockito.Mockito.when;

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
    @DisplayName("프로필 업데이트: 기존 이미지 삭제 후 새로운 이미지 업로드 ")
    void updateProfile_Success_WithImagerofile() throws IOException {
        //Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUserId(testUser.getId());
        request.setNickname("newNickname");
        MultipartFile newProfileImage = new MockMultipartFile("profileImage", "newImage.png", "image/png", "new image content".getBytes());

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(s3Service.isValidS3Url(testUser.getProfileImage())).thenReturn(true);
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