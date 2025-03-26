package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.TestSecurityConfig;
import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.repository.likes.LikesRepository;
import com.amumal.community.domain.post.service.likes.LikesService;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.config.security.JwtUserDetails;
import com.amumal.community.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LikesController.class)
@Import({TestSecurityConfig.class, LikesControllerTest.MockConfig.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {LikesController.class, TestSecurityConfig.class, LikesControllerTest.MockConfig.class, GlobalExceptionHandler.class})
public class LikesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LikesService likesService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikesRepository likesRepository;

    @Test
    @DisplayName("POST /posts/{postId}/likes - 좋아요 추가 성공")
    public void addLike_success() throws Exception {
        Long postId = 1L;
        // LikeRequest 생성 (필드가 있다면 적절히 설정)
        LikeRequest likeRequest = LikeRequest.builder()
                .postId(1L)
                .build();
        String requestJson = objectMapper.writeValueAsString(likeRequest);

        // UserService에서 현재 사용자 반환
        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(1L)).thenReturn(user);
        // likesService.addLike()는 void이므로 아무 동작 없이 처리되도록 설정
        doNothing().when(likesService).addLike(eq(postId), any(LikeRequest.class), eq(user));

        // JwtUserDetails 목 객체 생성
        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(post("/posts/{postId}/likes", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(user(jwtUserDetails))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("like_success"));
    }

    @Test
    @DisplayName("DELETE /posts/{postId}/likes - 좋아요 제거 성공")
    public void removeLike_success() throws Exception {
        Long postId = 1L;
        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(1L)).thenReturn(user);
        doNothing().when(likesService).removeLike(eq(postId), eq(user.getId()), eq(user));

        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(delete("/posts/{postId}/likes", postId)
                        .with(user(jwtUserDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("unlike_success"));
    }

    @Test
    @DisplayName("GET /posts/{postId}/likes/status - 좋아요 상태 조회 성공")
    public void checkLikeStatus_success() throws Exception {
        Long postId = 1L;
        // likesRepository.existsByPostIdAndUserId()가 true 반환하도록 설정
        when(likesRepository.existsByPostIdAndUserId(postId, 1L)).thenReturn(true);

        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(get("/posts/{postId}/likes/status", postId)
                        .with(user(jwtUserDetails))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public LikesService likesService() {
            return Mockito.mock(LikesService.class);
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public LikesRepository likesRepository() {
            return Mockito.mock(LikesRepository.class);
        }
    }
}
