package com.amumal.community.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CommentRequest(
        @NotBlank(message = "Content cannot be blank")
        String content
) {}
