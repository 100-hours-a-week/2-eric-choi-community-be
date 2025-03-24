package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.user.dto.request.PasswordUpdateRequest;
import com.amumal.community.domain.user.dto.request.UserUpdateRequest;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UserController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, UserControllerTest.MockConfig.class})
@ContextConfiguration(classes = {UserController.class, TestSecurityConfig.class, GlobalExceptionHandler.class, UserControllerTest.MockConfig.class})

class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("사용자 정보 조회 성공한다")
    public void getUserInfo_Success() throws Exception {
        Long userId = 1L;
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setEmail("user@example.com");
        userInfo.setNickname("TestUser");
        userInfo.setProfileImage("dummyProfileImage");

        when(userService.getUserInfo(userId)).thenReturn(userInfo);

        mockMvc.perform(get("/users/{id}", userId)
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("fetch_user_success"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("TestUser"))
                .andExpect(jsonPath("$.data.profileImage").value("dummyProfileImage"));
    }

    @Test
    @DisplayName("사용자 정보 조회 실패한다.")
    public void getUserInfo_Failure() throws Exception {
        Long userId = 1L;

        when(userService.getUserInfo(userId)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("server_error"));
    }

    @Test
    @DisplayName("프로필 업데이트 성공한다.")
    public void updateUserInfo_Success() throws Exception {
        Long userId = 1L;
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setNickname("UpdateUser");

        MockMultipartFile userInfoPart = new MockMultipartFile("userInfo", "",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(updateRequest));
        MockMultipartFile filePart = new MockMultipartFile("profileImage", "profile.png", MediaType.IMAGE_PNG_VALUE, "dummyContent".getBytes());

        JwtUserDetails dummyUser = Mockito.mock(JwtUserDetails.class);
        when(dummyUser.getId()).thenReturn(userId);

        // void 메서드 updateProfile를 stubbing 하여 아무 동작도 하지 않도록 설정
        Mockito.doNothing().when(userService).updateProfile(any(UserUpdateRequest.class), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/{id}", userId)
                        .file(userInfoPart)
                        .file(filePart)
                        .with(request -> { request.setMethod("PATCH"); return request; })
                        .with(csrf())
                        .with(user(dummyUser)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("profile_update_success"));
    }


    @Test
    @DisplayName("프로필 업데이트 인증 실패로 실패한다.")
    public void updateUserInfo_FailureWithUnauthorize() throws Exception {
        Long userId = 1L;
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUserId(2L);
        updateRequest.setNickname("UpdateUser");

        MockMultipartFile userInfoPart = new MockMultipartFile("userInfo", "",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(updateRequest));
        MockMultipartFile filePart = new MockMultipartFile("profileimage", "profile.png", MediaType.IMAGE_PNG_VALUE, "dummyContent".getBytes());

        JwtUserDetails dummyUser = Mockito.mock(JwtUserDetails.class);
        when(dummyUser.getId()).thenReturn(userId);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/{id}", userId)
                        .file(userInfoPart)
                        .file(filePart)
                        .with(request -> {request.setMethod("PATCH"); return request;})
                        .with(csrf())
                        .with(user(dummyUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("unauthorized_request"));
    }

    @Test
    @DisplayName("프로필 업데이트 예외처리로 실패한다.")
    public void updateUserInfo_FailureWithException() throws Exception {
        Long userId = 1L;
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setNickname("UpdateUser");

        MockMultipartFile userInfoPart = new MockMultipartFile("userInfo", "",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(updateRequest));
        MockMultipartFile filePart = new MockMultipartFile("profileimage", "profile.png", MediaType.IMAGE_PNG_VALUE, "dummyContent".getBytes());

        JwtUserDetails dummyUser = Mockito.mock(JwtUserDetails.class);
        when(dummyUser.getId()).thenReturn(userId);

        Mockito.doThrow(new RuntimeException("Update failed")).when(userService)
                .updateProfile(any(UserUpdateRequest.class), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/{id}", userId)
                        .file(userInfoPart)
                        .file(filePart)
                        .with(request -> {request.setMethod("PATCH"); return request;})
                        .with(csrf())
                        .with(user(dummyUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("server_error"));
    }

    @Test
    @DisplayName("비밀번호 업데이트 성공한다.")
    public void updatePassword_Success() throws Exception {
        Long userId = 1L;
        PasswordUpdateRequest passwordRequest = new PasswordUpdateRequest();
        passwordRequest.setUserId(userId);
        passwordRequest.setPassword("Password");
        passwordRequest.setConfirmPassword("Password");

        JwtUserDetails dummyUser = Mockito.mock(JwtUserDetails.class);
        when(dummyUser.getId()).thenReturn(userId);

        mockMvc.perform(patch("/users/{id}/password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest))
                        .with(csrf())
                        .with(user(dummyUser)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("password_update_success"));
    }

    @Test
    @DisplayName("인증 실패로 비밀번호 업데이트 실패한다.")
    public void updatePassword_FailureWithUnauthorize() throws Exception {
        Long userId = 1L;
        PasswordUpdateRequest passwordRequest = new PasswordUpdateRequest();
        passwordRequest.setUserId(2L);
        passwordRequest.setConfirmPassword("Password");
        passwordRequest.setPassword("Password");

        JwtUserDetails dummyUser = Mockito.mock(JwtUserDetails.class);
        when(dummyUser.getId()).thenReturn(userId);

        mockMvc.perform(patch("/users/{id}/password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest))
                        .with(csrf())
                        .with(user(dummyUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("unauthorized_request"));
    }

    @Test
    @DisplayName("서비스 예외로 비밀번호 업데이트 실패한다.")
    public void updatePassword_FailureWithException() throws Exception {
        Long userId = 1L;
        PasswordUpdateRequest passwordRequest = new PasswordUpdateRequest();
        passwordRequest.setUserId(userId);
        passwordRequest.setConfirmPassword("Password");
        passwordRequest.setPassword("Password");

        JwtUserDetails dummyUser = Mockito.mock(JwtUserDetails.class);
        when(dummyUser.getId()).thenReturn(userId);

        Mockito.doThrow(new RuntimeException("Update failed")).when(userService)
                .updateProfile(any(UserUpdateRequest.class), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest))
                        .with(csrf())
                        .with(user(dummyUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("server_error"));
    }

    @Test
    @DisplayName("사용자 회원 탈퇴 성공한다.")
    public void deleteUser_Success() throws Exception {
        Long userId = 1L;

        JwtUserDetails dummyUser = Mockito.mock(JwtUserDetails.class);
        when(dummyUser.getId()).thenReturn(userId);

        mockMvc.perform(delete("/users/{id}", userId)
                        .with(csrf())
                        .with(user(dummyUser)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("user_delete_success"));
    }

    @Test
    @DisplayName("인증 실패로 회원 사용자 회원 탈퇴 실패")
    public void deleteUser_FailureWithUnauthorize() throws Exception {
        Long userId = 1L;

        JwtUserDetails dummyUser = Mockito.mock(JwtUserDetails.class);
        when(dummyUser.getId()).thenReturn(2L); // 불일치

        mockMvc.perform(delete("/users/{id}", userId)
                        .with(csrf())
                        .with(user(dummyUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("unauthorized_request"));
    }

    @Test
    @DisplayName("사용자 삭제 실패 케이스 - 서비스 예외 발생")
    public void deleteUser_FailureWithException() throws Exception {
        Long userId = 1L;

        JwtUserDetails dummyUser = Mockito.mock(JwtUserDetails.class);
        when(dummyUser.getId()).thenReturn(userId);

        Mockito.doThrow(new RuntimeException("Delete failed")).when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{id}", userId)
                        .with(csrf())
                        .with(user(dummyUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("server_error"));
    }


    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }
}