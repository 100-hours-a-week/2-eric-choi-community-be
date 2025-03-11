package com.amumal.community.domain.post.service.post;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.user.entity.User;

public interface PostService {
    PostResponse getPostSimpleInfo(Long cursor, int pageSize);
    PostDetailResponse getPostDetailInfoById(Long postId);
    Long createPost(PostRequest request, User currentUser);
    void updatePost(Long postId, PostRequest request, User currentUser);
    void deletePost(Long postId, User currentUser);

    // 단순 조회용. Post 엔티티를 반환 (내부 사용)
    com.amumal.community.domain.post.entity.Post findById(Long postId);
}
