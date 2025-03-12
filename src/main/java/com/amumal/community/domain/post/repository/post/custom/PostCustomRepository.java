package com.amumal.community.domain.post.repository.post.custom;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import java.util.List;

public interface PostCustomRepository {
    PostDetailResponse getPostDetailInfoById(Long postId);
    List<PostSimpleInfo> getPostSimpleInfo(Long cursor, int pageSize);
}
