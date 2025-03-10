package com.amumal.community.domain.post.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LikeRequest(
        @NotNull(message = "Post ID cannot be null")
        Long postId
) {}
