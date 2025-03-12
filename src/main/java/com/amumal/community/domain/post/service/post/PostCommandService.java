package com.amumal.community.domain.post.service.post;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.user.entity.User;

public interface PostCommandService {
    Long createPost(PostRequest request, User currentUser);
    void updatePost(Long postId, PostRequest request, User currentUser);
    void deletePost(Long postId, User currentUser);
}