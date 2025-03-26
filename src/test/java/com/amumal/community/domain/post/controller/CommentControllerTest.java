package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.service.comment.CommentService;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.config.security.JwtUserDetails;
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

    @Test
    @DisplayName("POST /posts/{postId}/comments - 댓글 생성 성공")
    public void createComment_success() throws Exception {
        Long postId = 1L;
        // CommentRequest 생성 (예: content 필드)
        CommentRequest commentRequest = CommentRequest.builder()
                .content("Test comment")
                .build();
        String requestJson = objectMapper.writeValueAsString(commentRequest);

        // UserService에서 현재 사용자 반환
        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(1L)).thenReturn(user);

        // commentService.createComment()가 100L 반환하도록 설정
        when(commentService.createComment(eq(postId), any(CommentRequest.class), eq(user)))
                .thenReturn(100L);

        // JwtUserDetails 목 객체 생성
        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

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
        Long postId = 1L;
        Long commentId = 200L;
        CommentRequest commentRequest = CommentRequest.builder()
                .content("Updated comment")
                .build();
        String requestJson = objectMapper.writeValueAsString(commentRequest);

        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(1L)).thenReturn(user);

        // commentService.updateComment()는 void이므로 doNothing() 처리
        doNothing().when(commentService).updateComment(eq(postId), eq(commentId), any(CommentRequest.class), eq(user));

        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

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
        Long postId = 1L;
        Long commentId = 300L;

        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(1L)).thenReturn(user);

        doNothing().when(commentService).deleteComment(eq(postId), eq(commentId), eq(user));

        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}", postId, commentId)
                        .with(user(jwtUserDetails))
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("delete_comment_success"));
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
