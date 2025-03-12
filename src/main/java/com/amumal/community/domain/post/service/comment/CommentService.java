package com.amumal.community.domain.post.service.comment;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.entity.Comment;
import com.amumal.community.domain.user.entity.User;

public interface CommentService {
    Long createComment(Long postId, CommentRequest request, User currentUser);
    void updateComment(Long postId, Long commentId, CommentRequest request, User currentUser);
    void deleteComment(Long postId, Long commentId, User currentUser);
}
