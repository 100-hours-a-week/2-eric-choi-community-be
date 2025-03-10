package com.amumal.community.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 10)
    private String nickname;

    @NotBlank
    @Size(max = 1000)
    private String profileImage;
}
