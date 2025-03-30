package com.amumal.community.domain.post.service.likes.impl;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.entity.Likes;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.likes.LikesRepository;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.user.entity.User;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikesServiceImplTest {

    // 테스트 상수
    private static final Long POST_ID = 1L;
    private static final Long USER_ID = 1L;

    @Mock
    private LikesRepository likesRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private LikesServiceImpl likesService;

    // 공통 변수
    private User testUser;
    private LikeRequest likeRequest;
    private Post testPost;
    private Likes testLike;

    @BeforeEach
    void setUp() {
        // 기본 모의 객체 설정
        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(USER_ID);

        likeRequest = mock(LikeRequest.class);
        testPost = mock(Post.class);
        testLike = mock(Likes.class);
    }

    @Nested
    @DisplayName("좋아요 추가 테스트")
    class AddLikeTest {

        @Test
        @DisplayName("좋아요가 이미 있으면 제거")
        void addLike_likeExists_removesLike() {
            // Given
            when(likesRepository.existsByPostIdAndUserId(POST_ID, USER_ID)).thenReturn(true);
            when(likesRepository.findByPostIdAndUserId(POST_ID, USER_ID)).thenReturn(Optional.of(testLike));

            // When
            likesService.addLike(POST_ID, likeRequest, testUser);

            // Then
            verify(likesRepository).existsByPostIdAndUserId(POST_ID, USER_ID);
            verify(likesRepository).findByPostIdAndUserId(POST_ID, USER_ID);
            verify(likesRepository).delete(testLike);
            verify(postRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("좋아요가 없으면 새로 추가")
        void addLike_likeNotExists_savesNewLike() {
            // Given
            when(likesRepository.existsByPostIdAndUserId(POST_ID, USER_ID)).thenReturn(false);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));

            // When
            likesService.addLike(POST_ID, likeRequest, testUser);

            // Then
            verify(likesRepository).existsByPostIdAndUserId(POST_ID, USER_ID);
            verify(postRepository).findById(POST_ID);
            verify(likesRepository).save(argThat(like ->
                    like.getPost() == testPost && like.getUser() == testUser
            ));
        }
    }

    @Nested
    @DisplayName("좋아요 제거 테스트")
    class RemoveLikeTest {

        @Test
        @DisplayName("좋아요가 없으면 예외 발생")
        void removeLike_likeNotExists_throwsException() {
            // Given
            when(likesRepository.findByPostIdAndUserId(POST_ID, USER_ID)).thenReturn(Optional.empty());

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> likesService.removeLike(POST_ID, USER_ID, testUser));

            assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
            verify(likesRepository).findByPostIdAndUserId(POST_ID, USER_ID);
        }

        @Test
        @DisplayName("좋아요가 있으면 제거 성공")
        void removeLike_likeExists_success() {
            // Given
            when(likesRepository.findByPostIdAndUserId(POST_ID, USER_ID)).thenReturn(Optional.of(testLike));

            // When
            likesService.removeLike(POST_ID, USER_ID, testUser);

            // Then
            verify(likesRepository).findByPostIdAndUserId(POST_ID, USER_ID);
            verify(likesRepository).delete(testLike);
        }
    }
}