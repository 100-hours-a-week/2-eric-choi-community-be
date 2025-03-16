package com.amumal.community.domain.post.entity;

import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.global.entity.BaseEntity;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    // 작성자: User 엔티티와 다대일 관계
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "titles", length = 30, nullable = false)
    private String title;

    @Lob
    @Column(name = "contents", nullable = false)
    private String content;

    @Lob
    @Column(name = "img", columnDefinition = "LONGTEXT")
    private String image;

    @Column(name = "view", nullable = false)
    private int viewCount;

    // 댓글: 일대다 관계 (Cascade, orphanRemoval 적용)
    @OneToMany(mappedBy = "post", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 좋아요: 일대다 관계 (Cascade, orphanRemoval 적용)
    @OneToMany(mappedBy = "post", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    // BaseEntity의 onCreate() 후 추가 초기화
    @Override
    protected void onCreate() {
        super.onCreate();  // createdAt, updatedAt 설정
        this.viewCount = 0;
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 게시글 수정 메서드.
     * 접근한 사용자가 작성자와 일치하는지 검증한 후 제목, 내용, 이미지 정보를 업데이트합니다.
     *
     * @param modifyRequest 수정 요청 DTO
     * @param accessUser 접근한 사용자(User)
     */
    public void update(PostRequest modifyRequest, User accessUser) {
        validateSameUser(accessUser);
        this.title = modifyRequest.title();
        this.content = modifyRequest.content();
        this.image = modifyRequest.image();
    }

    /**
     * 게시글 삭제(논리 삭제) 메서드.
     * 접근한 사용자가 작성자와 일치하는지 검증한 후 논리 삭제 처리(BaseEntity.delete())를 수행합니다.
     *
     * @param accessUser 접근한 사용자(User)
     */
    public void safeDelete(User accessUser) {
        validateSameUser(accessUser);
        this.delete();
    }

    /**
     * 작성자 검증 메서드.
     * 접근한 사용자의 ID와 게시글 작성자의 ID가 다르면 예외를 발생시킵니다.
     *
     * @param accessUser 접근한 사용자(User)
     */
    public void validateSameUser(User accessUser) {
        if (!Objects.equals(this.user.getId(), accessUser.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }
    }
}
