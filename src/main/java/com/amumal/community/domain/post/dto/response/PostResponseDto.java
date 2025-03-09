package com.amumal.community.domain.post.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostResponseDto {

    private Long postId;
    private String title;
    private String content;
    private int likeCount;
    private int commentCount;
    private int viewCount;
    private String authorNickname;
    private String authorProfileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostResponseDto(Long postId, String title, String content, int likeCount, int commentCount, int viewCount,
                           String authorNickname, String authorProfileImage, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.authorNickname = authorNickname;
        this.authorProfileImage = authorProfileImage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
