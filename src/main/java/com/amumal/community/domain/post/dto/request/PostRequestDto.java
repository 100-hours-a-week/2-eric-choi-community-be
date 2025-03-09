package com.amumal.community.domain.post.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {

    private String title;
    private String content;
    private Long userId; // 작성자 (User ID)

}
