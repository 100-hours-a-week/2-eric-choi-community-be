package com.amumal.community.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
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

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profileImage;
}
