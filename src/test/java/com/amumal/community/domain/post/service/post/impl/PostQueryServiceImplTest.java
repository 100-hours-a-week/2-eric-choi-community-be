package com.amumal.community.domain.post.service.post.impl;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceImplTest {

    // 테스트 상수
    private static final Long POST_ID = 1L;
    private static final Long CURSOR = 0L;
    private static final int PAGE_SIZE = 10;
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private PostQueryServiceImpl postQueryService;

    @Nested
    @DisplayName("게시글 목록 조회 테스트")
    class GetPostListTest {

        @Test
        @DisplayName("게시글이 없을 때 단순 정보 조회 시 빈 응답 반환")
        void getSimpleInfo_NoPosts_ReturnsEmptyResponse() {
            // Given
            when(postRepository.getPostSimpleInfo(anyLong(), anyInt())).thenReturn(Collections.emptyList());

            // When
            PostResponse response = postQueryService.getPostSimpleInfo(CURSOR, PAGE_SIZE);

            // Then
            assertNotNull(response);
            assertTrue(response.postSimpleInfos().isEmpty());
            assertNull(response.nextCursor());
            verify(postRepository).getPostSimpleInfo(CURSOR, PAGE_SIZE);
        }

        @Test
        @DisplayName("게시글이 있을 때 단순 정보 조회 시 next cursor 설정")
        void getSimpleInfo_WithPosts_ReturnsResponseWithNextCursor() {
            // Given
            List<PostSimpleInfo> simpleInfos = List.of(
                    new PostSimpleInfo(1L, "Title1", null, 0, 0, 0, "User1", "Profile1"),
                    new PostSimpleInfo(2L, "Title2", null, 5, 3, 15, "User2", "Profile2")
            );
            when(postRepository.getPostSimpleInfo(CURSOR, PAGE_SIZE)).thenReturn(simpleInfos);

            // When
            PostResponse response = postQueryService.getPostSimpleInfo(CURSOR, PAGE_SIZE);

            // Then
            assertNotNull(response);
            assertEquals(2, response.postSimpleInfos().size());
            assertEquals(2L, response.nextCursor());
            verify(postRepository).getPostSimpleInfo(CURSOR, PAGE_SIZE);
        }
    }

    @Nested
    @DisplayName("게시글 상세 조회 테스트")
    class GetPostDetailTest {

        private Post mockPost;
        private PostDetailResponse mockDetailResponse;

        @BeforeEach
        void setUp() {
            // 모의 Post 객체 생성
            mockPost = mock(Post.class);

            // 모의 DetailResponse 생성
            mockDetailResponse = PostDetailResponse.builder().build();
        }

        @Test
        @DisplayName("게시글 존재, incrementView true 시 조회수 증가 후 상세 정보 반환")
        void getDetail_PostExists_IncrementTrue_ReturnsDetailAfterIncrement() {
            // Given
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(mockPost));
            when(postRepository.getPostDetailInfoById(POST_ID)).thenReturn(mockDetailResponse);

            // When
            PostDetailResponse result = postQueryService.getPostDetailInfoById(POST_ID, true);

            // Then
            assertNotNull(result);
            verify(mockPost).incrementViewCount();
            verify(postRepository).save(mockPost);
            verify(postRepository).getPostDetailInfoById(POST_ID);
        }

        @Test
        @DisplayName("존재하지 않는 게시글, incrementView true 시 예외 발생")
        void getDetail_PostNotExists_IncrementTrue_ThrowsException() {
            // Given
            when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> postQueryService.getPostDetailInfoById(POST_ID, true));
            assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
            verify(postRepository, never()).getPostDetailInfoById(anyLong());
        }

        @Test
        @DisplayName("조회수 증가 없이 상세 정보 조회 시 정상 응답 반환")
        void getDetail_IncrementFalse_ReturnsDetailWithoutIncrement() {
            // Given
            when(postRepository.getPostDetailInfoById(POST_ID)).thenReturn(mockDetailResponse);

            // When
            PostDetailResponse result = postQueryService.getPostDetailInfoById(POST_ID, false);

            // Then
            assertNotNull(result);
            verify(postRepository, never()).findById(anyLong());
            verify(postRepository, never()).save(any(Post.class));
            verify(postRepository).getPostDetailInfoById(POST_ID);
        }

        @Test
        @DisplayName("상세 정보가 null이면 예외 발생")
        void getDetail_DetailNull_ThrowsException() {
            // Given
            when(postRepository.getPostDetailInfoById(POST_ID)).thenReturn(null);

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> postQueryService.getPostDetailInfoById(POST_ID, false));
            assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
        }

        @Test
        @DisplayName("incrementView null 시 조회수 증가 없이 상세 정보 반환")
        void getDetail_IncrementNull_ReturnsDetailWithoutIncrement() {
            // Given
            when(postRepository.getPostDetailInfoById(POST_ID)).thenReturn(mockDetailResponse);

            // When
            PostDetailResponse result = postQueryService.getPostDetailInfoById(POST_ID, null);

            // Then
            assertNotNull(result);
            verify(postRepository, never()).findById(anyLong());
            verify(postRepository, never()).save(any(Post.class));
            verify(postRepository).getPostDetailInfoById(POST_ID);
        }
    }
}