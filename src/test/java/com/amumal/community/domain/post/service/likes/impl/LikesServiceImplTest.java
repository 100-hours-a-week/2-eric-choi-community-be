package com.amumal.community.domain.post.service.likes.impl;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.entity.Likes;
import com.amumal.community.domain.post.repository.likes.LikesRepository;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.post.service.likes.LikesService;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private LikesRepository likesRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private LikesServiceImpl likesService;

    @Test
    @DisplayName("좋아요가 이미 있으면 좋아요 제거")
    void addLike_alreadyExists_thenRemove() {
        Long postId = 1L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        LikeRequest req = mock(LikeRequest.class);

        when(likesRepository.existsByPostIdAndUserId(postId, 1L)).thenReturn(true);
        Likes existingLike = mock(Likes.class);
        when(likesRepository.findByPostIdAndUserId(postId, 1L)).thenReturn(Optional.of(existingLike));

        likesService.addLike(postId, req, user);

        verify(likesRepository, times(1)).delete(existingLike);
        verify(postRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("좋아요가 없으면 새 좋아요 추가")
    void addLike_notExists_thenSave() {
        Long postId = 1L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        LikeRequest req = mock(LikeRequest.class);

        when(likesRepository.existsByPostIdAndUserId(postId, 1L)).thenReturn(false);
        var post = mock(com.amumal.community.domain.post.entity.Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        likesService.addLike(postId, req, user);

        verify(likesRepository, times(1)).save(argThat(like ->
                like.getPost() == post && like.getUser() == user
        ));
    }

    @Test
    @DisplayName("좋아요 제거 - 좋아요 없으면 예외 발생")
    void removeLike_notExists_thenException() {
        Long postId = 1L;
        Long userId = 1L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(likesRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () ->
                likesService.removeLike(postId, userId, user)
        );
        assertEquals(CustomResponseStatus.NOT_FOUND, ex.getStatus());
    }
}
