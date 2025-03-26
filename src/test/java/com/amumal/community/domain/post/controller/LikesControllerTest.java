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
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.ResultActions;

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

    // 테스트 상수
    private static final Long POST_ID = 1L;
    private static final Long USER_ID = 1L;
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
    // 테스트 공통 변수
    private User testUser;
    private JwtUserDetails jwtUserDetails;
    private LikeRequest likeRequest;
    private String requestJson;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트 사용자 설정
        testUser = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(USER_ID)).thenReturn(testUser);

        // JWT 사용자 설정
        jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(USER_ID);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        // 좋아요 요청 객체 설정
        likeRequest = LikeRequest.builder()
                .postId(POST_ID)
                .build();
        requestJson = objectMapper.writeValueAsString(likeRequest);
    }

    /**
     * 인증된 요청을 수행하는 헬퍼 메서드
     */
    private ResultActions performAuthenticatedRequest(String method, String url, Object content) throws Exception {
        switch (method) {
            case "POST":
                return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
                        .with(user(jwtUserDetails))
                        .with(csrf()));
            case "DELETE":
                return mockMvc.perform(delete(url)
                        .with(user(jwtUserDetails))
                        .with(csrf()));
            case "GET":
                return mockMvc.perform(get(url)
                        .with(user(jwtUserDetails))
                        .with(csrf()));
            default:
                throw new IllegalArgumentException("지원하지 않는 HTTP 메서드: " + method);
        }
    }

    @Test
    @DisplayName("POST /posts/{postId}/likes - 좋아요 추가 성공")
    public void addLike_success() throws Exception {
        // Given
        doNothing().when(likesService).addLike(eq(POST_ID), any(LikeRequest.class), eq(testUser));

        // When & Then
        performAuthenticatedRequest("POST", "/posts/" + POST_ID + "/likes", likeRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("like_success"));
    }

    @Test
    @DisplayName("DELETE /posts/{postId}/likes - 좋아요 제거 성공")
    public void removeLike_success() throws Exception {
        // Given
        doNothing().when(likesService).removeLike(eq(POST_ID), eq(USER_ID), eq(testUser));

        // When & Then
        performAuthenticatedRequest("DELETE", "/posts/" + POST_ID + "/likes", null)
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("unlike_success"));
    }

    @Test
    @DisplayName("GET /posts/{postId}/likes/status - 좋아요 상태 조회 성공")
    public void checkLikeStatus_success() throws Exception {
        // Given
        when(likesRepository.existsByPostIdAndUserId(POST_ID, USER_ID)).thenReturn(true);

        // When & Then
        performAuthenticatedRequest("GET", "/posts/" + POST_ID + "/likes/status", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 좋아요 추가 시도 시 실패")
    public void addLike_unauthorized_fails() throws Exception {
        // When & Then (인증 정보 없이 요청)
        mockMvc.perform(post("/posts/" + POST_ID + "/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
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