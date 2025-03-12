package com.amumal.community.domain.post.service.likes;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.user.entity.User;

public interface LikesService {
    void addLike(Long postId, LikeRequest request, User currentUser);
    void removeLike(Long postId, Long userId, User currentUser);
}
