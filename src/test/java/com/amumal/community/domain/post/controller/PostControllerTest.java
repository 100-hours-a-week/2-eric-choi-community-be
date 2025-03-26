package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.TestSecurityConfig;
import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.service.post.PostCommandService;
import com.amumal.community.domain.post.service.post.PostQueryService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@Import({TestSecurityConfig.class, PostControllerTest.MockConfig.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {PostController.class, TestSecurityConfig.class, PostControllerTest.MockConfig.class, GlobalExceptionHandler.class})
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostQueryService postQueryService;

    @Autowired
    private PostCommandService postCommandService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("GET /posts - 게시글 목록 조회")
    public void getPosts_success() throws Exception {
        PostResponse response = PostResponse.builder()
                .postSimpleInfos(Collections.emptyList())
                .nextCursor(null)
                .build();
        when(postQueryService.getPostSimpleInfo(null, 10)).thenReturn(response);

        mockMvc.perform(get("/posts")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("fetch_posts_success"))
                .andExpect(jsonPath("$.data.nextCursor").isEmpty());
    }

    @Test
    @DisplayName("GET /posts/{postId} - 게시글 상세 조회")
    public void getPostDetail_success() throws Exception {
        PostDetailResponse detailResponse = PostDetailResponse.builder()
                .postId(1L)
                .title("Test Post")
                .content("Test Content")
                .image("http://example.com/image.jpg")
                .createdAt(null)
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .author(null)
                .comments(Collections.emptyList())
                .build();
        when(postQueryService.getPostDetailInfoById(1L, true)).thenReturn(detailResponse);

        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(get("/posts/1")
                        .param("incrementView", "true")
                        .with(user(jwtUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("fetch_post_detail_success"))
                .andExpect(jsonPath("$.data.postId").value(1));
    }

    @Test
    @DisplayName("POST /posts - 게시글 생성")
    public void createPost_success() throws Exception {
        PostRequest req = PostRequest.builder()
                .title("제목")
                .content("내용")
                .image(null)
                .build();
        MockMultipartFile postInfo = new MockMultipartFile(
                "postInfo", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req)
        );
        MockMultipartFile image = new MockMultipartFile(
                "image", "image.jpg", MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );
        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(1L)).thenReturn(user);
        when(postCommandService.createPost(any(PostRequest.class), any(), eq(user))).thenReturn(1L);

        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(multipart("/posts")
                        .file(postInfo)
                        .file(image)
                        .with(user(jwtUserDetails))  // 올바른 principal 제공
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("create_post_success"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("POST /posts - 게시글 생성 실패: 제목이 비어있음")
    public void createPost_failure_blankTitle() throws Exception {
        // 제목은 빈 문자열, content는 정상 값을 가진 JSON 문자열 생성
        String json = "{\"title\":\"\",\"content\":\"내용\",\"image\":null}";
        MockMultipartFile postInfo = new MockMultipartFile(
                "postInfo", "postInfo.json", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/posts")
                        .file(postInfo)
                        .with(csrf())
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Title cannot be blank")));
    }

    @Test
    @DisplayName("POST /posts - 게시글 생성 실패: 제목 길이 초과")
    public void createPost_failure_titleExceedingLength() throws Exception {
        // 제목은 31자, content는 정상 값을 가진 JSON 문자열 생성
        String longTitle = "1234567890123456789012345678901"; // 31자
        String json = String.format("{\"title\":\"%s\",\"content\":\"내용\",\"image\":null}", longTitle);
        MockMultipartFile postInfo = new MockMultipartFile(
                "postInfo", "postInfo.json", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/posts")
                        .file(postInfo)
                        .with(csrf())
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Title must not exceed 30 characters")));
    }

    @Test
    @DisplayName("POST /posts - 게시글 생성 실패: 내용이 비어있음")
    public void createPost_failure_blankContent() throws Exception {
        // content가 빈 문자열인 JSON 생성 (제목은 정상)
        String json = "{\"title\":\"제목\",\"content\":\"\",\"image\":null}";
        MockMultipartFile postInfo = new MockMultipartFile(
                "postInfo", "postInfo.json", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/posts")
                        .file(postInfo)
                        .with(csrf())
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Content cannot be blank")));
    }


    @Test
    @DisplayName("PATCH /posts/{postId} - 게시글 수정")
    public void updatePost_success() throws Exception {
        PostRequest req = PostRequest.builder()
                .title("제목")
                .content("내용")
                .image(null)  // 또는 이미지 URL을 지정할 수 있습니다.
                .build();
        MockMultipartFile postInfo = new MockMultipartFile(
                "postInfo", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(req)
        );
        // 이미지 파일이 없는 경우 빈 파일 전송
        MockMultipartFile image = new MockMultipartFile(
                "image", "", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]
        );
        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(1L)).thenReturn(user);


        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(multipart("/posts/1")
                        .file(postInfo)
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH"); // multipart 요청에서 HTTP 메서드 오버라이드
                            return request;
                        })
                        .with(user(jwtUserDetails))  // 올바른 principal 제공
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("update_post_success"));

    }

    @Test
    @DisplayName("DELETE /posts/{postId} - 게시글 삭제")
    public void deletePost_success() throws Exception {
        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .build();
        when(userService.findById(1L)).thenReturn(user);

        JwtUserDetails jwtUserDetails = Mockito.mock(JwtUserDetails.class);
        when(jwtUserDetails.getId()).thenReturn(1L);
        when(jwtUserDetails.getUsername()).thenReturn("test@test.com");

        mockMvc.perform(delete("/posts/1")
                        .with(user(jwtUserDetails))  // 올바른 principal 제공
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("delete_post_success"));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public PostQueryService postQueryService() {
            return Mockito.mock(PostQueryService.class);
        }

        @Bean
        public PostCommandService postCommandService() {
            return Mockito.mock(PostCommandService.class);
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }
}
