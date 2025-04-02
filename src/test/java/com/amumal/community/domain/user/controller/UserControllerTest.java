package com.amumal.community.domain.user.controller;

import com.amumal.community.TestSecurityConfig;
import com.amumal.community.domain.user.dto.request.PasswordUpdateRequest;
import com.amumal.community.domain.user.dto.request.UserUpdateRequest;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.config.security.JwtUserDetails;
import com.amumal.community.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, UserControllerTest.MockConfig.class})
@ContextConfiguration(classes = {UserController.class, TestSecurityConfig.class, GlobalExceptionHandler.class, UserControllerTest.MockConfig.class})
class UserControllerTest {

    // 테스트 상수
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final String EMAIL = "user@example.com";
    private static final String NICKNAME = "TestUser";
    private static final String UPDATED_NICKNAME = "UpdateUser";
    private static final String PROFILE_IMAGE = "dummyProfileImage";
    private static final String PASSWORD = "Password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        reset(userService);
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }

    @Nested
    @DisplayName("사용자 정보 조회 테스트")
    class GetUserInfoTest {

        @Test
        @DisplayName("사용자 정보 조회 성공")
        void getUserInfo_success() throws Exception {
            // Given
            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setId(USER_ID); // ID 설정 추가
            userInfo.setEmail(EMAIL);
            userInfo.setNickname(NICKNAME);
            userInfo.setProfileImage(PROFILE_IMAGE);

            when(userService.getUserInfo(USER_ID)).thenReturn(userInfo);

            // When & Then
            mockMvc.perform(get("/users/{id}", USER_ID)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("fetch_user_success"))
                    .andExpect(jsonPath("$.data.email").value(EMAIL))
                    .andExpect(jsonPath("$.data.nickname").value(NICKNAME))
                    .andExpect(jsonPath("$.data.profileImage").value(PROFILE_IMAGE));
        }

        @Test
        @DisplayName("사용자 정보 조회 실패")
        void getUserInfo_failure() throws Exception {
            // Given
            when(userService.getUserInfo(USER_ID)).thenThrow(new RuntimeException("User not found"));

            // When & Then
            mockMvc.perform(get("/users/{id}", USER_ID)
                            .with(csrf()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("server_error"));
        }
    }

    @Nested
    @DisplayName("프로필 업데이트 테스트")
    class UpdateProfileTest {

        private UserUpdateRequest createUpdateRequest(Long userId) {
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setUserId(userId);
            updateRequest.setNickname(UPDATED_NICKNAME);
            return updateRequest;
        }

        private JwtUserDetails createJwtUserDetails(Long userId) {
            JwtUserDetails userDetails = mock(JwtUserDetails.class);
            when(userDetails.getId()).thenReturn(userId);
            return userDetails;
        }

        private MockMultipartFile createUserInfoPart(UserUpdateRequest request) throws Exception {
            return new MockMultipartFile("userInfo", "",
                    MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));
        }

        private MockMultipartFile createProfileImagePart() {
            return new MockMultipartFile("profileImage", "profile.png",
                    MediaType.IMAGE_PNG_VALUE, "dummyContent".getBytes());
        }

        @Test
        @DisplayName("프로필 업데이트 성공")
        void updateUserInfo_success() throws Exception {
            // Given
            UserUpdateRequest updateRequest = createUpdateRequest(USER_ID);
            MockMultipartFile userInfoPart = createUserInfoPart(updateRequest);
            MockMultipartFile filePart = createProfileImagePart();
            JwtUserDetails dummyUser = createJwtUserDetails(USER_ID);

            doNothing().when(userService).updateProfile(any(UserUpdateRequest.class), any());

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.multipart("/users/{id}", USER_ID)
                            .file(userInfoPart)
                            .file(filePart)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                            .with(csrf())
                            .with(user(dummyUser)))
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.message").value("profile_update_success"));
        }

        @Test
        @DisplayName("프로필 업데이트 인증 실패")
        void updateUserInfo_unauthorized_fails() throws Exception {
            // Given: 요청 userId와 인증된 userId가 다름
            UserUpdateRequest updateRequest = createUpdateRequest(OTHER_USER_ID);
            MockMultipartFile userInfoPart = createUserInfoPart(updateRequest);
            MockMultipartFile filePart = createProfileImagePart();
            JwtUserDetails dummyUser = createJwtUserDetails(USER_ID);

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.multipart("/users/{id}", USER_ID)
                            .file(userInfoPart)
                            .file(filePart)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                            .with(csrf())
                            .with(user(dummyUser)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("unauthorized_request"));
        }

        @Test
        @DisplayName("프로필 업데이트 예외 발생")
        void updateUserInfo_exception_fails() throws Exception {
            // Given
            UserUpdateRequest updateRequest = createUpdateRequest(USER_ID);
            MockMultipartFile userInfoPart = createUserInfoPart(updateRequest);
            MockMultipartFile filePart = createProfileImagePart();
            JwtUserDetails dummyUser = createJwtUserDetails(USER_ID);

            doThrow(new RuntimeException("Update failed")).when(userService)
                    .updateProfile(any(UserUpdateRequest.class), any());

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.multipart("/users/{id}", USER_ID)
                            .file(userInfoPart)
                            .file(filePart)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                            .with(csrf())
                            .with(user(dummyUser)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("server_error"));
        }
    }

    @Nested
    @DisplayName("비밀번호 업데이트 테스트")
    class UpdatePasswordTest {

        private PasswordUpdateRequest createPasswordRequest(Long userId) {
            PasswordUpdateRequest request = new PasswordUpdateRequest();
            request.setUserId(userId);
            request.setPassword(PASSWORD);
            request.setConfirmPassword(PASSWORD);
            return request;
        }

        private JwtUserDetails createJwtUserDetails(Long userId) {
            JwtUserDetails userDetails = mock(JwtUserDetails.class);
            when(userDetails.getId()).thenReturn(userId);
            return userDetails;
        }

        @Test
        @DisplayName("비밀번호 업데이트 성공")
        void updatePassword_success() throws Exception {
            // Given
            PasswordUpdateRequest passwordRequest = createPasswordRequest(USER_ID);
            JwtUserDetails dummyUser = createJwtUserDetails(USER_ID);

            // When & Then
            mockMvc.perform(patch("/users/{id}/password", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordRequest))
                            .with(csrf())
                            .with(user(dummyUser)))
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.message").value("password_update_success"));
        }

        @Test
        @DisplayName("비밀번호 업데이트 인증 실패")
        void updatePassword_unauthorized_fails() throws Exception {
            // Given: 요청 userId와 인증된 userId가 다름
            PasswordUpdateRequest passwordRequest = createPasswordRequest(OTHER_USER_ID);
            JwtUserDetails dummyUser = createJwtUserDetails(USER_ID);

            // When & Then
            mockMvc.perform(patch("/users/{id}/password", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordRequest))
                            .with(csrf())
                            .with(user(dummyUser)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("unauthorized_request"));
        }
    }

    @Nested
    @DisplayName("사용자 삭제 테스트")
    class DeleteUserTest {

        private JwtUserDetails createJwtUserDetails(Long userId) {
            JwtUserDetails userDetails = mock(JwtUserDetails.class);
            when(userDetails.getId()).thenReturn(userId);
            return userDetails;
        }

        @Test
        @DisplayName("사용자 삭제 성공")
        void deleteUser_success() throws Exception {
            // Given
            JwtUserDetails dummyUser = createJwtUserDetails(USER_ID);

            // 중요: 이 부분을 추가해야 합니다!
            doNothing().when(userService).deleteUser(USER_ID);

            // When & Then
            mockMvc.perform(delete("/users/{id}", USER_ID)
                            .with(csrf())
                            .with(user(dummyUser)))
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.message").value("user_delete_success"));
        }

        @Test
        @DisplayName("사용자 삭제 인증 실패")
        void deleteUser_unauthorized_fails() throws Exception {
            // Given: 요청 userId와 인증된 userId가 다름
            JwtUserDetails dummyUser = createJwtUserDetails(OTHER_USER_ID);

            // When & Then
            mockMvc.perform(delete("/users/{id}", USER_ID)
                            .with(csrf())
                            .with(user(dummyUser)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("unauthorized_request"));
        }

        @Test
        @DisplayName("사용자 삭제 예외 발생")
        void deleteUser_exception_fails() throws Exception {
            // Given
            JwtUserDetails dummyUser = createJwtUserDetails(USER_ID);
            doThrow(new RuntimeException("Delete failed")).when(userService).deleteUser(USER_ID);

            // When & Then
            mockMvc.perform(delete("/users/{id}", USER_ID)
                            .with(csrf())
                            .with(user(dummyUser)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("server_error"));
        }
    }
}