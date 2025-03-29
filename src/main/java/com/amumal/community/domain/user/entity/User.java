package com.amumal.community.domain.user.entity;

import com.amumal.community.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity implements Serializable { // BaseEntity 상속 추가
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "nick_name", length = 10, nullable = false, unique = true)
    private String nickname;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Lob
    @Column(name = "profile_image", columnDefinition = "LONGTEXT", nullable = false)
    @Builder.Default
    private String profileImage = "";

    // 사용자 프로필 업데이트 메서드
    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    // 비밀번호 업데이트 메서드
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // BaseEntity에 이미 delete() 메서드가 있으므로 제거
}