package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.service.comment.CommentService;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.config.security.JwtUserDetails;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
@Import({CommentControllerTest.MockConfig.class})
@ContextConfiguration(classes = {CommentController.class, CommentControllerTest.MockConfig.class})
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    // 공통 변수 선언
    private User testUser;
    private JwtUserDetails jwtUserDetails;
    private CommentRequest commentRequest;
    private String requestJson;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트 유저 설정
        testUser = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(1L)).thenReturn(testUser);

        // JwtUserDetails 설정
        jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        // 댓글 요청 객체 생성
        commentRequest = CommentRequest.builder()
                .content("Test comment")
                .build();
        requestJson = objectMapper.writeValueAsString(commentRequest);
    }

    @Test
    @DisplayName("POST /posts/{postId}/comments - 댓글 생성 성공")
    public void createComment_success() throws Exception {
        // Given
        Long postId = 1L;
        when(commentService.createComment(eq(postId), any(CommentRequest.class), eq(testUser)))
                .thenReturn(100L);

        // When & Then
        mockMvc.perform(post("/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(user(jwtUserDetails))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("create_comment_success"))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @DisplayName("PATCH /posts/{postId}/comments/{commentId} - 댓글 수정 성공")
    public void updateComment_success() throws Exception {
        // Given
        Long postId = 1L;
        Long commentId = 200L;
        // commentService.updateComment()는 void이므로 doNothing() 처리
        doNothing().when(commentService).updateComment(eq(postId), eq(commentId), any(CommentRequest.class), eq(testUser));

        // When & Then
        mockMvc.perform(patch("/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(user(jwtUserDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("update_comment_success"));
    }

    @Test
    @DisplayName("DELETE /posts/{postId}/comments/{commentId} - 댓글 삭제 성공")
    public void deleteComment_success() throws Exception {
        // Given
        Long postId = 1L;
        Long commentId = 300L;
        doNothing().when(commentService).deleteComment(eq(postId), eq(commentId), eq(testUser));

        // When & Then
        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}", postId, commentId)
                        .with(user(jwtUserDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("delete_comment_success"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 댓글 생성 시도 시 실패")
    public void createComment_unauthorized_fails() throws Exception {
        // Given
        Long postId = 1L;

        // When & Then (인증 정보 없이 요청)
        mockMvc.perform(post("/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public CommentService commentService() {
            return Mockito.mock(CommentService.class);
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }
}