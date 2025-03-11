package com.amumal.community.domain.post.service.comment;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.entity.Comment;
import com.amumal.community.domain.post.repository.comment.CommentRepository;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.post.service.post.PostService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService; // to fetch Post entity for association

    @Override
    public Long createComment(Long postId, CommentRequest request, User currentUser) {
        // PostService.findById() retrieves the Post entity.
        Comment comment = Comment.builder()
                .post(postService.findById(postId))
                .user(currentUser)
                .content(request.content())
                .build();
        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }

    @Override
    public void updateComment(Long postId, Long commentId, CommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST));
        // updateComment 내부에서 작성자 검증이 수행됩니다.
        comment.updateComment(request, currentUser);
    }

    @Override
    public void deleteComment(Long postId, Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST));
        comment.safeDelete(currentUser);
    }
}