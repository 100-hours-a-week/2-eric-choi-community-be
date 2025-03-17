package com.amumal.community.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PostRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 30, message = "Title must not exceed 30 characters")
        String title,

        @NotBlank(message = "Content cannot be blank")
        String content,

        String image
) {}
