package com.amumal.community.domain.post.service.comment.impl;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.entity.Comment;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.comment.CommentRepository;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("게시글 존재하면 댓글 생성 후 ID 반환")
    void createComment_success() {
        //Given
        Long postId = 1L;
        CommentRequest req = mock(CommentRequest.class);
        when(req.content()).thenReturn("댓글 내용");
        User currentUser = mock(User.class);
        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            when(saved.getId()).thenReturn(100L);
            return saved;
        });

        //When
        Long resultId = commentService.createComment(postId, req, currentUser);

        //Then
        assertEquals(100L, resultId);
        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

}