package com.amumal.community.domain.post.service.comment.impl;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.entity.Comment;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.comment.CommentRepository;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
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
            Comment realComment = invocation.getArgument(0);
            Comment spyComment = spy(realComment);
            when(spyComment.getId()).thenReturn(100L);
            return spyComment;
        });

        //When
        Long resultId = commentService.createComment(postId, req, currentUser);

        //Then
        assertEquals(100L, resultId);
        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("게시글 없어서 댓글 생성 실패")
    void createComment_failWithNoPst() {
        //Given
        Long postId = 1L;
        CommentRequest req = mock(CommentRequest.class);
        User currentUser = mock(User.class);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        //When&Then
        CustomException ex = assertThrows(CustomException.class, () -> {
            commentService.createComment(postId, req, currentUser);
        });
        assertEquals(CustomResponseStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_success() {
        // Given
        Long postId = 1L;
        Long commentId = 2L;
        CommentRequest req = mock(CommentRequest.class);
        User currentUser = mock(User.class);
        Comment comment = mock(Comment.class);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        // When
        commentService.updateComment(postId, commentId, req, currentUser);
        // Then
        verify(comment, times(1)).updateComment(req, currentUser);
    }

    @Test
    @DisplayName("댓글 없어서 댓글 수정 실패")
    void updateComment_failWithNoComment() {
        // Given
        Long postId = 1L;
        Long commentId = 2L;
        CommentRequest req = mock(CommentRequest.class);
        User currentUser = mock(User.class);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
        // When & Then
        CustomException ex = assertThrows(CustomException.class, () ->
                commentService.updateComment(postId, commentId, req, currentUser)
        );
        assertEquals(CustomResponseStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_success() {
        // Given
        Long postId = 1L;
        Long commentId = 2L;
        User currentUser = mock(User.class);
        Comment comment = mock(Comment.class);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        // When
        commentService.deleteComment(postId, commentId, currentUser);
        // Then
        verify(comment, times(1)).safeDelete(currentUser);
    }

    @Test
    @DisplayName("댓글이 없어서 댓글 삭제 실패")
    void deleteComment_failWithNoComment() {
        // Given
        Long postId = 1L;
        Long commentId = 2L;
        User currentUser = mock(User.class);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
        // When & Then
        CustomException ex = assertThrows(CustomException.class, () ->
                commentService.deleteComment(postId, commentId, currentUser)
        );
        assertEquals(CustomResponseStatus.NOT_FOUND, ex.getStatus());
    }
}
