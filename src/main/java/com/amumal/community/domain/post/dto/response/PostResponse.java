package com.amumal.community.domain.post.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostResponse(
        List<PostSimpleInfo> postSimpleInfos,
        Long nextCursor
) {
    @Builder
    public record PostSimpleInfo(
            Long postId,
            String title,
            LocalDateTime createdAt,
            Integer likeCount,
            Integer commentCount,
            Integer viewCount,
            String authorNickname,
            String authorProfileImg
    ) {}
}
