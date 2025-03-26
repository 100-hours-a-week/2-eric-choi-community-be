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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    // 테스트 상수
    private static final Long POST_ID = 1L;
    private static final Long COMMENT_ID = 2L;
    private static final Long RESULT_COMMENT_ID = 100L;
    private static final String COMMENT_CONTENT = "댓글 내용";

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Nested
    @DisplayName("댓글 생성 테스트")
    class CreateCommentTest {

        @Test
        @DisplayName("게시글 존재하면 댓글 생성 후 ID 반환")
        void createComment_postExists_returnsCommentId() {
            // Given
            User testUser = mock(User.class);
            Post testPost = mock(Post.class);
            CommentRequest commentRequest = mock(CommentRequest.class);
            when(commentRequest.content()).thenReturn(COMMENT_CONTENT);

            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(testPost));
            when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
                Comment spyComment = mock(Comment.class);
                when(spyComment.getId()).thenReturn(RESULT_COMMENT_ID);
                return spyComment;
            });

            // When
            Long resultId = commentService.createComment(POST_ID, commentRequest, testUser);

            // Then
            assertEquals(RESULT_COMMENT_ID, resultId);
            verify(postRepository).findById(POST_ID);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("게시글 없으면 댓글 생성 실패")
        void createComment_postNotExists_throwsException() {
            // Given
            User testUser = mock(User.class);
            CommentRequest commentRequest = mock(CommentRequest.class);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> commentService.createComment(POST_ID, commentRequest, testUser));

            assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
            verify(postRepository).findById(POST_ID);
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class UpdateCommentTest {

        @Test
        @DisplayName("댓글 존재하면 수정 성공")
        void updateComment_commentExists_success() {
            // Given
            User testUser = mock(User.class);
            Comment testComment = mock(Comment.class);
            CommentRequest commentRequest = mock(CommentRequest.class);

            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(testComment));

            // When
            commentService.updateComment(POST_ID, COMMENT_ID, commentRequest, testUser);

            // Then
            verify(commentRepository).findById(COMMENT_ID);
            verify(testComment).updateComment(commentRequest, testUser);
        }

        @Test
        @DisplayName("댓글 없으면 수정 실패")
        void updateComment_commentNotExists_throwsException() {
            // Given
            User testUser = mock(User.class);
            CommentRequest commentRequest = mock(CommentRequest.class);

            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> commentService.updateComment(POST_ID, COMMENT_ID, commentRequest, testUser));

            assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
            verify(commentRepository).findById(COMMENT_ID);
        }
    }

    @Nested
    @DisplayName("댓글 삭제 테스트")
    class DeleteCommentTest {

        @Test
        @DisplayName("댓글 존재하면 삭제 성공")
        void deleteComment_commentExists_success() {
            // Given
            User testUser = mock(User.class);
            Comment testComment = mock(Comment.class);

            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(testComment));

            // When
            commentService.deleteComment(POST_ID, COMMENT_ID, testUser);

            // Then
            verify(commentRepository).findById(COMMENT_ID);
            verify(testComment).safeDelete(testUser);
        }

        @Test
        @DisplayName("댓글 없으면 삭제 실패")
        void deleteComment_commentNotExists_throwsException() {
            // Given
            User testUser = mock(User.class);

            when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> commentService.deleteComment(POST_ID, COMMENT_ID, testUser));

            assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
            verify(commentRepository).findById(COMMENT_ID);
        }
    }
}