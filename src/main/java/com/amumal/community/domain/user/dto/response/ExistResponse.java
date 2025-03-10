package com.amumal.community.domain.user.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExistResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileImage;
}
