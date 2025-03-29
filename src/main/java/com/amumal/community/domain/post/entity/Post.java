package com.amumal.community.domain.post.entity;

import com.amumal.community.domain.user.entity.User;
import com.amumal.community.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    // 댓글: 일대다 관계 (Cascade, orphanRemoval 적용)
    @OneToMany(mappedBy = "post", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 좋아요: 일대다 관계 (Cascade, orphanRemoval 적용)
    @OneToMany(mappedBy = "post", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @Column(name = "title", length = 30, nullable = false)
    private String title;

    @Lob
    @Column(name = "contents", nullable = false)
    private String content;

    @Lob
    @Column(name = "img", columnDefinition = "LONGTEXT")
    private String image;

    @Column(name = "view", nullable = false)
    private int viewCount;

    // BaseEntity의 onCreate() 후 추가 초기화
    @Override
    protected void onCreate() {
        super.onCreate();  // createdAt, updatedAt 설정
        this.viewCount = 0;
    }

    // 조회수 증가 메서드 - 유지 (단순한 상태 변경)
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 게시글 내용 업데이트 메서드 - 유지 (단순한 상태 변경)
    public void updateContent(String title, String content, String image) {
        this.title = title;
        this.content = content;
        this.image = image;
    }
}