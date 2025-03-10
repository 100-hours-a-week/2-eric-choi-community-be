package com.amumal.community.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
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
}