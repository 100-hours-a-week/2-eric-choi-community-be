package com.amumal.community.domain.user.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String nickname;
    private String email;
    private String profileImage;
    private String accessToken;
    private String refreshToken;
}
