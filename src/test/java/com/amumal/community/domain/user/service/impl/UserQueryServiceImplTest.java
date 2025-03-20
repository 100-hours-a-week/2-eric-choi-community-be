package com.amumal.community.domain.user.service.impl;

import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.repository.UserRepository;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserQueryServiceImpl userQueryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 User 엔티티 생성 (빌더 패턴에 따라 수정)
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("tester")
                .profileImage("http://example.com/profile.png")
                .build();
    }

    @Test
    @DisplayName("유저 정보 조회 성공: 존재하는 ID")
    void getUserInfoById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserInfoResponse response = userQueryService.getUserInfoById(1L);

        // Then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getNickname(), response.getNickname());
        assertEquals(testUser.getProfileImage(), response.getProfileImage());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("유저 정보 조회 실패: 존재하지 않는 ID")
    void getUserInfoById_Failure() {
        // Given: 조회 시 Optional.empty() 반환
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then: CustomException 발생 확인
        CustomException exception = assertThrows(CustomException.class,
                () -> userQueryService.getUserInfoById(1L));
        assertEquals(CustomResponseStatus.NOT_FOUND, exception.getErrorStatus());
        verify(userRepository).findById(1L);
    }


    @Test
    @DisplayName("이메일 중복 확인: 중복된 경우")
    void isEmailDuplicate_True() {
        // Given: 이메일이 중복된 경우 true 반환
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean isDuplicate = userQueryService.isEmailDuplicate("test@example.com");

        // Then
        assertTrue(isDuplicate);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("이메일 중복 확인: 중복되지 않은 경우")
    void isEmailDuplicate_False() {
        // Given: 중복되지 않은 경우 false 반환
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        // When
        boolean isDuplicate = userQueryService.isEmailDuplicate("test@example.com");

        // Then
        assertFalse(isDuplicate);
        verify(userRepository).existsByEmail("test@example.com");
    }


    @Test
    @DisplayName("닉네임 중복 확인: 중복된 경우")
    void isNicknameDuplicate_True() {
        // Given
        when(userRepository.existsByNickname("tester")).thenReturn(true);

        // When
        boolean isDuplicate = userQueryService.isNicknameDuplicate("tester");

        // Then
        assertTrue(isDuplicate);
        verify(userRepository).existsByNickname("tester");
    }

    @Test
    @DisplayName("닉네임 중복 확인: 중복되지 않은 경우")
    void isNicknameDuplicate_False() {
        // Given
        when(userRepository.existsByNickname("tester")).thenReturn(false);

        // When
        boolean isDuplicate = userQueryService.isNicknameDuplicate("tester");

        // Then
        assertFalse(isDuplicate);
        verify(userRepository).existsByNickname("tester");
    }


    @Test
    @DisplayName("이메일로 유저 조회 성공")
    void getUserByEmail_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User foundUser = userQueryService.getUserByEmail("test@example.com");

        // Then
        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("이메일로 유저 조회 실패")
    void getUserByEmail_Failure() {
        // Given: 해당 이메일이 존재하지 않음
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then: CustomException 발생 확인
        CustomException exception = assertThrows(CustomException.class,
                () -> userQueryService.getUserByEmail("test@example.com"));
        assertEquals(CustomResponseStatus.NOT_FOUND, exception.getErrorStatus());
        verify(userRepository).findByEmail("test@example.com");
    }
}
