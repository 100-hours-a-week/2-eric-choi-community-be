package com.amumal.community.domain.post.entity;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.global.entity.BaseEntity;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글 작성자: User 엔티티와 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 해당 댓글이 속한 게시글: Post 엔티티와 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 댓글 수정 메서드.
     * 수정 요청 DTO와 접근한 사용자(User)를 받아 작성자 검증 후 내용을 업데이트합니다.
     *
     * @param modifyRequest 수정 요청 DTO
     * @param accessUser 접근한 사용자(User)
     */
    public void updateComment(CommentRequest modifyRequest, User accessUser) {
        validateSameUser(accessUser);
        this.content = modifyRequest.content();
    }

    /**
     * 댓글 삭제(논리 삭제) 메서드.
     * 작성자 검증 후 BaseEntity의 delete() 메서드를 호출합니다.
     *
     * @param accessUser 접근한 사용자(User)
     */
    public void safeDelete(User accessUser) {
        validateSameUser(accessUser);
        this.delete();
    }

    /**
     * 작성자 검증 메서드.
     * 접근한 사용자의 ID와 댓글 작성자의 ID가 다르면 예외를 발생시킵니다.
     *
     * @param accessUser 접근한 사용자(User)
     */
    public void validateSameUser(User accessUser) {
        if (!Objects.equals(this.user.getId(), accessUser.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }
    }
}
