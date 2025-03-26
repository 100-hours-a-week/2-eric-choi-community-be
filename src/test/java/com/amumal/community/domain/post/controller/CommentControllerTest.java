package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.service.comment.CommentService;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.config.security.JwtUserDetails;
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

@WebMvcTest(controllers = CommentController.class)
@Import({CommentControllerTest.MockConfig.class})
@ContextConfiguration(classes = {CommentController.class, CommentControllerTest.MockConfig.class})
class CommentControllerTest {

    // 테스트 상수
    private static final Long POST_ID = 1L;
    private static final Long COMMENT_ID = 200L;
    private static final Long NEW_COMMENT_ID = 100L;
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@test.com";
    private static final String USER_NICKNAME = "TestUser";
    private static final String USER_PASSWORD = "password";
    private static final String COMMENT_CONTENT = "Test comment";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    // 테스트용 공통 변수
    private User testUser;
    private JwtUserDetails jwtUserDetails;
    private CommentRequest commentRequest;
    private String commentRequestJson;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트 유저 설정
        testUser = User.builder()
                .nickname(USER_NICKNAME)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .build();

        when(userService.findById(USER_ID)).thenReturn(testUser);

        // JwtUserDetails 설정
        jwtUserDetails = mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(USER_ID);
        when(jwtUserDetails.getUsername()).thenReturn(USER_EMAIL);

        // 댓글 요청 객체 생성
        commentRequest = CommentRequest.builder()
                .content(COMMENT_CONTENT)
                .build();

        commentRequestJson = objectMapper.writeValueAsString(commentRequest);
    }

    /**
     * HTTP 요청을 수행하는 헬퍼 메서드
     */
    private ResultActions performAuthenticatedRequest(String method, String urlTemplate, Object... urlVariables) throws Exception {
        switch (method) {
            case "POST":
                return mockMvc.perform(post(urlTemplate, urlVariables)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequestJson)
                        .with(user(jwtUserDetails))
                        .with(csrf()));
            case "PATCH":
                return mockMvc.perform(patch(urlTemplate, urlVariables)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequestJson)
                        .with(user(jwtUserDetails))
                        .with(csrf()));
            case "DELETE":
                return mockMvc.perform(delete(urlTemplate, urlVariables)
                        .with(user(jwtUserDetails))
                        .with(csrf()));
            default:
                throw new IllegalArgumentException("지원하지 않는 HTTP 메서드: " + method);
        }
    }

    @Nested
    @DisplayName("댓글 생성 테스트")
    class CreateCommentTest {

        @Test
        @DisplayName("인증된 사용자의 댓글 생성 요청 성공")
        void createComment_authenticated_success() throws Exception {
            // Given
            when(commentService.createComment(eq(POST_ID), any(CommentRequest.class), eq(testUser)))
                    .thenReturn(NEW_COMMENT_ID);

            // When & Then
            performAuthenticatedRequest("POST", "/posts/{postId}/comments", POST_ID)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("create_comment_success"))
                    .andExpect(jsonPath("$.data").value(NEW_COMMENT_ID));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 댓글 생성 요청 실패")
        void createComment_unauthenticated_fails() throws Exception {
            // When & Then: 인증 정보 없이 요청
            mockMvc.perform(post("/posts/{postId}/comments", POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(commentRequestJson)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class UpdateCommentTest {

        @Test
        @DisplayName("인증된 사용자의 댓글 수정 요청 성공")
        void updateComment_authenticated_success() throws Exception {
            // Given
            doNothing().when(commentService).updateComment(
                    eq(POST_ID), eq(COMMENT_ID), any(CommentRequest.class), eq(testUser));

            // When & Then
            performAuthenticatedRequest("PATCH", "/posts/{postId}/comments/{commentId}", POST_ID, COMMENT_ID)
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.message").value("update_comment_success"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 댓글 수정 요청 실패")
        void updateComment_unauthenticated_fails() throws Exception {
            // When & Then: 인증 정보 없이 요청
            mockMvc.perform(patch("/posts/{postId}/comments/{commentId}", POST_ID, COMMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(commentRequestJson)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("댓글 삭제 테스트")
    class DeleteCommentTest {

        @Test
        @DisplayName("인증된 사용자의 댓글 삭제 요청 성공")
        void deleteComment_authenticated_success() throws Exception {
            // Given
            doNothing().when(commentService).deleteComment(eq(POST_ID), eq(COMMENT_ID), eq(testUser));

            // When & Then
            performAuthenticatedRequest("DELETE", "/posts/{postId}/comments/{commentId}", POST_ID, COMMENT_ID)
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.message").value("delete_comment_success"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 댓글 삭제 요청 실패")
        void deleteComment_unauthenticated_fails() throws Exception {
            // When & Then: 인증 정보 없이 요청
            mockMvc.perform(delete("/posts/{postId}/comments/{commentId}", POST_ID, COMMENT_ID)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public CommentService commentService() {
            return mock(CommentService.class);
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }
}