package com.amumal.community.domain.post.controller;

import com.amumal.community.TestSecurityConfig;
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@Import({TestSecurityConfig.class, PostControllerTest.MockConfig.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {PostController.class, TestSecurityConfig.class, PostControllerTest.MockConfig.class, GlobalExceptionHandler.class})
class PostControllerTest {

    // 테스트 상수
    private static final Long POST_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@test.com";
    private static final String USER_NICKNAME = "TestUser";
    private static final String USER_PASSWORD = "password";
    private static final String POST_TITLE = "제목";
    private static final String POST_CONTENT = "내용";
    private static final String LONG_TITLE = "1234567890123456789012345678901"; // 31자
    private static final int PAGE_SIZE = 10;

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

    // 테스트용 공통 변수
    private User testUser;
    private JwtUserDetails jwtUserDetails;

    @BeforeEach
    void setUp() {
        // 테스트 유저 설정
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
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public PostQueryService postQueryService() {
            return mock(PostQueryService.class);
        }

        @Bean
        public PostCommandService postCommandService() {
            return mock(PostCommandService.class);
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }
    }

    @Nested
    @DisplayName("게시글 목록 조회 테스트")
    class GetPostsTest {

        @Test
        @DisplayName("게시글 목록 조회 성공")
        void getPosts_success() throws Exception {
            // Given
            PostResponse response = PostResponse.builder()
                    .postSimpleInfos(Collections.emptyList())
                    .nextCursor(null)
                    .build();

            when(postQueryService.getPostSimpleInfo(null, PAGE_SIZE)).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/posts")
                            .param("pageSize", String.valueOf(PAGE_SIZE)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("fetch_posts_success"))
                    .andExpect(jsonPath("$.data.nextCursor").isEmpty());
        }
    }

    @Nested
    @DisplayName("게시글 상세 조회 테스트")
    class GetPostDetailTest {

        @Test
        @DisplayName("게시글 상세 조회 성공")
        void getPostDetail_success() throws Exception {
            // Given
            PostDetailResponse detailResponse = PostDetailResponse.builder()
                    .postId(POST_ID)
                    .title(POST_TITLE)
                    .content(POST_CONTENT)
                    .image("http://example.com/image.jpg")
                    .createdAt(null)
                    .viewCount(0)
                    .likeCount(0)
                    .commentCount(0)
                    .author(null)
                    .comments(Collections.emptyList())
                    .build();

            when(postQueryService.getPostDetailInfoById(POST_ID, true)).thenReturn(detailResponse);

            // When & Then
            mockMvc.perform(get("/posts/{postId}", POST_ID)
                            .param("incrementView", "true")
                            .with(user(jwtUserDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("fetch_post_detail_success"))
                    .andExpect(jsonPath("$.data.postId").value(POST_ID));
        }
    }

    @Nested
    @DisplayName("게시글 생성 테스트")
    class CreatePostTest {

        private PostRequest createPostRequest() {
            return PostRequest.builder()
                    .title(POST_TITLE)
                    .content(POST_CONTENT)
                    .image(null)
                    .build();
        }

        private MockMultipartFile createPostInfoFile(PostRequest request) throws Exception {
            return new MockMultipartFile(
                    "postInfo", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );
        }

        private MockMultipartFile createDummyImageFile() {
            return new MockMultipartFile(
                    "image", "image.jpg", MediaType.IMAGE_JPEG_VALUE,
                    "dummy image content".getBytes()
            );
        }

        @Test
        @DisplayName("게시글 생성 성공")
        void createPost_success() throws Exception {
            // Given
            PostRequest req = createPostRequest();
            MockMultipartFile postInfo = createPostInfoFile(req);
            MockMultipartFile image = createDummyImageFile();

            when(postCommandService.createPost(any(PostRequest.class), any(), eq(testUser))).thenReturn(POST_ID);

            // When & Then
            mockMvc.perform(multipart("/posts")
                            .file(postInfo)
                            .file(image)
                            .with(user(jwtUserDetails))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("create_post_success"))
                    .andExpect(jsonPath("$.data").value(POST_ID));
        }

        @Test
        @DisplayName("게시글 생성 실패: 제목이 비어있음")
        void createPost_blankTitle_fails() throws Exception {
            // Given: 제목은 빈 문자열, content는 정상 값을 가진 JSON 문자열 생성
            String json = "{\"title\":\"\",\"content\":\"내용\",\"image\":null}";
            MockMultipartFile postInfo = new MockMultipartFile(
                    "postInfo", "postInfo.json", MediaType.APPLICATION_JSON_VALUE,
                    json.getBytes(StandardCharsets.UTF_8)
            );

            // When & Then
            mockMvc.perform(multipart("/posts")
                            .file(postInfo)
                            .with(csrf())
                            .characterEncoding("UTF-8"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("Title cannot be blank")));
        }

        @Test
        @DisplayName("게시글 생성 실패: 제목 길이 초과")
        void createPost_titleExceedingLength_fails() throws Exception {
            // Given: 제목은 31자, content는 정상 값을 가진 JSON 문자열 생성
            String json = String.format("{\"title\":\"%s\",\"content\":\"내용\",\"image\":null}", LONG_TITLE);
            MockMultipartFile postInfo = new MockMultipartFile(
                    "postInfo", "postInfo.json", MediaType.APPLICATION_JSON_VALUE,
                    json.getBytes(StandardCharsets.UTF_8)
            );

            // When & Then
            mockMvc.perform(multipart("/posts")
                            .file(postInfo)
                            .with(csrf())
                            .characterEncoding("UTF-8"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("Title must not exceed 30 characters")));
        }

        @Test
        @DisplayName("게시글 생성 실패: 내용이 비어있음")
        void createPost_blankContent_fails() throws Exception {
            // Given: content가 빈 문자열인 JSON 생성 (제목은 정상)
            String json = "{\"title\":\"제목\",\"content\":\"\",\"image\":null}";
            MockMultipartFile postInfo = new MockMultipartFile(
                    "postInfo", "postInfo.json", MediaType.APPLICATION_JSON_VALUE,
                    json.getBytes(StandardCharsets.UTF_8)
            );

            // When & Then
            mockMvc.perform(multipart("/posts")
                            .file(postInfo)
                            .with(csrf())
                            .characterEncoding("UTF-8"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("Content cannot be blank")));
        }
    }

    @Nested
    @DisplayName("게시글 수정 테스트")
    class UpdatePostTest {

        private PostRequest createUpdateRequest() {
            return PostRequest.builder()
                    .title(POST_TITLE)
                    .content(POST_CONTENT)
                    .image(null)
                    .build();
        }

        private MockMultipartFile createPostInfoFile(PostRequest request) throws Exception {
            return new MockMultipartFile(
                    "postInfo", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );
        }

        private MockMultipartFile createEmptyImageFile() {
            return new MockMultipartFile(
                    "image", "", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]
            );
        }

        @Test
        @DisplayName("게시글 수정 성공")
        void updatePost_success() throws Exception {
            // Given
            PostRequest req = createUpdateRequest();
            MockMultipartFile postInfo = createPostInfoFile(req);
            MockMultipartFile image = createEmptyImageFile();

            // When & Then
            mockMvc.perform(multipart("/posts/{postId}", POST_ID)
                            .file(postInfo)
                            .file(image)
                            .with(request -> {
                                request.setMethod("PATCH"); // multipart 요청에서 HTTP 메서드 오버라이드
                                return request;
                            })
                            .with(user(jwtUserDetails))
                            .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.message").value("update_post_success"));
        }
    }

    @Nested
    @DisplayName("게시글 삭제 테스트")
    class DeletePostTest {

        @Test
        @DisplayName("게시글 삭제 성공")
        void deletePost_success() throws Exception {
            // When & Then
            mockMvc.perform(delete("/posts/{postId}", POST_ID)
                            .with(user(jwtUserDetails))
                            .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.message").value("delete_post_success"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 게시글 삭제 실패")
        void deletePost_unauthenticated_fails() throws Exception {
            // When & Then (인증 정보 없이 요청)
            mockMvc.perform(delete("/posts/{postId}", POST_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}