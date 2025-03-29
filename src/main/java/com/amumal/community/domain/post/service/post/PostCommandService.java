package com.amumal.community.domain.post.service.post;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface PostCommandService {
    Long createPost(PostRequest request, MultipartFile image, User currentUser);
    void updatePost(Long postId, PostRequest request, MultipartFile image, User currentUser);
    void deletePost(Long postId, User currentUser);
}