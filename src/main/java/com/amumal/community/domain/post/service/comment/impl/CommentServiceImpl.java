package com.amumal.community.domain.post.service.comment.impl;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.entity.Comment;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.comment.CommentRepository;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.post.service.comment.CommentService;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    public Long createComment(Long postId, CommentRequest request, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
        Comment comment = Comment.builder()
                .post(post)
                .user(currentUser)
                .content(request.content())
                .build();
        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }

    @Override
    public void updateComment(Long postId, Long commentId, CommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
        comment.updateComment(request, currentUser);
    }

    @Override
    public void deleteComment(Long postId, Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
        comment.safeDelete(currentUser);
    }
}
