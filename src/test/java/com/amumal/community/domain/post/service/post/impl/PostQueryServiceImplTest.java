package com.amumal.community.domain.post.service.post.impl;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostQueryServiceImpl postQueryService;

    @Test
    @DisplayName("게시글이 없을 때 단순 정보 조회 시 빈 응답 반환")
    void noPosts_getSimpleInfo_empty() {
        // Given
        when(postRepository.getPostSimpleInfo(anyLong(), anyInt())).thenReturn(Collections.emptyList());
        // When
        PostResponse response = postQueryService.getPostSimpleInfo(0L, 10);
        // Then
        assertNotNull(response);
        assertTrue(response.postSimpleInfos().isEmpty());
        assertNull(response.nextCursor());
        verify(postRepository, times(1)).getPostSimpleInfo(0L, 10);
    }

    @Test
    @DisplayName("게시글이 있을 때 단순 정보 조회 시 nextCursor 설정")
    void posts_getSimpleInfo_nextCursor() {
        // Given
        List<PostSimpleInfo> simpleInfos = List.of(
                new PostSimpleInfo(1L, "Title1", null, 0, 0, 0, "User1", "Profile1"),
                new PostSimpleInfo(2L, "Title2", null, 5, 3, 15, "User2", "Profile2")
        );
        when(postRepository.getPostSimpleInfo(0L, 10)).thenReturn(simpleInfos);
        // When
        PostResponse response = postQueryService.getPostSimpleInfo(0L, 10);
        // Then
        assertNotNull(response);
        assertEquals(2, response.postSimpleInfos().size());
        assertEquals(2L, response.nextCursor());
        verify(postRepository, times(1)).getPostSimpleInfo(0L, 10);
    }

    @Test
    @DisplayName("게시글 존재, incrementView true 시 조회수 증가 후 상세 정보 반환")
    void existingPost_incTrue_getDetail() {
        Long postId = 1L;
        // Given
        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        PostDetailResponse detailResponse = PostDetailResponse.builder().build();
        when(postRepository.getPostDetailInfoById(postId)).thenReturn(detailResponse);
        // When
        PostDetailResponse result = postQueryService.getPostDetailInfoById(postId, true);
        // Then
        assertNotNull(result);
        verify(post, times(1)).incrementViewCount();
        verify(postRepository, times(1)).save(post);
    }

    @Test
    @DisplayName("존재하지 않는 게시글, incrementView true 시 예외 발생")
    void nonExistPost_incTrue_exception() {
        Long postId = 1L;
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postQueryService.getPostDetailInfoById(postId, true));
        assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    @DisplayName("조회수 증가 없이 상세 정보 조회 시 정상 응답 반환")
    void validDetail_noInc_getDetail() {
        Long postId = 1L;
        // Given
        PostDetailResponse detailResponse = PostDetailResponse.builder().build();
        when(postRepository.getPostDetailInfoById(postId)).thenReturn(detailResponse);
        // When
        PostDetailResponse result = postQueryService.getPostDetailInfoById(postId, false);
        // Then
        assertNotNull(result);
        verify(postRepository, never()).findById(anyLong());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("상세 정보가 null이면 예외 발생")
    void nullDetail_exception() {
        Long postId = 1L;
        // Given
        when(postRepository.getPostDetailInfoById(postId)).thenReturn(null);
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postQueryService.getPostDetailInfoById(postId, false));
        assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    @DisplayName("incrementView null 시 조회수 증가 없이 상세 정보 반환")
    void validDetail_nullInc_getDetail() {
        Long postId = 1L;
        // Given
        PostDetailResponse detailResponse = PostDetailResponse.builder().build();
        when(postRepository.getPostDetailInfoById(postId)).thenReturn(detailResponse);
        // When
        PostDetailResponse result = postQueryService.getPostDetailInfoById(postId, null);
        // Then
        assertNotNull(result);
        verify(postRepository, never()).findById(anyLong());
        verify(postRepository, never()).save(any(Post.class));
    }
}
