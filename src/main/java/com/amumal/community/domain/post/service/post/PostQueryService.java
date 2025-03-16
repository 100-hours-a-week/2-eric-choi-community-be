package com.amumal.community.domain.post.service.post;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;

public interface PostQueryService {
    PostResponse getPostSimpleInfo(Long cursor, int pageSize);
    PostDetailResponse getPostDetailInfoById(Long postId, Boolean incrementView);
}