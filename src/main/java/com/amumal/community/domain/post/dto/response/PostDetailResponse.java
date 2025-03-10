package com.amumal.community.domain.post.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        LocalDateTime createdAt,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        AuthorInfo author,
        List<CommentResponse> comments
) {
    @Builder
    public record AuthorInfo(
            String nickname,
            String profileImage
    ) {}

    @Builder
    public record CommentResponse(
            Long commentId,
            String content,
            LocalDateTime createdAt,
            AuthorInfo author
    ) {}
}
