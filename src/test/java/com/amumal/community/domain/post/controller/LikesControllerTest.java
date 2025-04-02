package com.amumal.community.domain.post.controller;

import com.amumal.community.TestSecurityConfig;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LikesController.class)
@Import({TestSecurityConfig.class, LikesControllerTest.MockConfig.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {LikesController.class, TestSecurityConfig.class, LikesControllerTest.MockConfig.class, GlobalExceptionHandler.class})
class LikesControllerTest {

    // 테스트 상수
    private static final Long POST_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@test.com";
    private static final String USER_NICKNAME = "TestUser";
    private static final String USER_PASSWORD = "password";

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

    // 테스트용 공통 변수
    private User testUser;
    private JwtUserDetails jwtUserDetails;
    private LikeRequest likeRequest;
    private String likeRequestJson;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트 사용자 설정
        testUser = User.builder()
                .nickname(USER_NICKNAME)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .build();

        when(userService.findById(USER_ID)).thenReturn(testUser);

        // JWT 사용자 설정
        jwtUserDetails = mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(USER_ID);
        when(jwtUserDetails.getUsername()).thenReturn(USER_EMAIL);

        // 좋아요 요청 객체 설정
        likeRequest = LikeRequest.builder()
                .postId(POST_ID)
                .build();

        likeRequestJson = objectMapper.writeValueAsString(likeRequest);
    }

    /**
     * 인증된 요청을 수행하는 헬퍼 메서드
     */
    private ResultActions performAuthenticatedRequest(String method, String urlTemplate, Object... urlVariables) throws Exception {
        switch (method) {
            case "POST":
                return mockMvc.perform(post(urlTemplate, urlVariables)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(likeRequestJson)
                        .with(user(jwtUserDetails))
                        .with(csrf()));
            case "DELETE":
                return mockMvc.perform(delete(urlTemplate, urlVariables)
                        .with(user(jwtUserDetails))
                        .with(csrf()));
            case "GET":
                return mockMvc.perform(get(urlTemplate, urlVariables)
                        .with(user(jwtUserDetails))
                        .with(csrf()));
            default:
                throw new IllegalArgumentException("지원하지 않는 HTTP 메서드: " + method);
        }
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public LikesService likesService() {
            return mock(LikesService.class);
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public LikesRepository likesRepository() {
            return mock(LikesRepository.class);
        }
    }

    @Nested
    @DisplayName("좋아요 추가 테스트")
    class AddLikeTest {

        @Test
        @DisplayName("인증된 사용자의 좋아요 추가 요청 성공")
        void addLike_authenticated_success() throws Exception {
            // Given
            doNothing().when(likesService).addLike(eq(POST_ID), any(LikeRequest.class), eq(testUser));

            // When & Then
            performAuthenticatedRequest("POST", "/posts/{postId}/likes", POST_ID)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("like_success"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 좋아요 추가 요청 실패")
        void addLike_unauthenticated_fails() throws Exception {
            // When & Then (인증 정보 없이 요청)
            mockMvc.perform(post("/posts/{postId}/likes", POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(likeRequestJson)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("좋아요 제거 테스트")
    class RemoveLikeTest {

        @Test
        @DisplayName("인증된 사용자의 좋아요 제거 요청 성공")
        void removeLike_authenticated_success() throws Exception {
            // Given
            doNothing().when(likesService).removeLike(eq(POST_ID), eq(USER_ID), eq(testUser));

            // When & Then
            performAuthenticatedRequest("DELETE", "/posts/{postId}/likes", POST_ID)
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.message").value("unlike_success"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 좋아요 제거 요청 실패")
        void removeLike_unauthenticated_fails() throws Exception {
            // When & Then (인증 정보 없이 요청)
            mockMvc.perform(delete("/posts/{postId}/likes", POST_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("좋아요 상태 확인 테스트")
    class CheckLikeStatusTest {

        @Test
        @DisplayName("인증된 사용자의 좋아요 상태 조회 성공 - 좋아요 있음")
        void checkLikeStatus_authenticated_liked_returnsTrue() throws Exception {
            // Given
            when(likesRepository.existsByPostIdAndUserId(POST_ID, USER_ID)).thenReturn(true);

            // When & Then
            performAuthenticatedRequest("GET", "/posts/{postId}/likes/status", POST_ID)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("인증된 사용자의 좋아요 상태 조회 성공 - 좋아요 없음")
        void checkLikeStatus_authenticated_notLiked_returnsFalse() throws Exception {
            // Given
            when(likesRepository.existsByPostIdAndUserId(POST_ID, USER_ID)).thenReturn(false);

            // When & Then
            performAuthenticatedRequest("GET", "/posts/{postId}/likes/status", POST_ID)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 좋아요 상태 조회 실패")
        void checkLikeStatus_unauthenticated_fails() throws Exception {
            // When & Then (인증 정보 없이 요청)
            mockMvc.perform(get("/posts/{postId}/likes/status", POST_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}