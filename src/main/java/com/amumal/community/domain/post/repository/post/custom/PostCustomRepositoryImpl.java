package com.amumal.community.domain.post.repository.post.custom;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import com.amumal.community.domain.post.entity.Post;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;

import static com.amumal.community.domain.post.entity.QPost.post;
import static com.amumal.community.domain.post.entity.QComment.comment;
import static com.amumal.community.domain.post.entity.QLikes.likes;
import static com.amumal.community.domain.user.entity.QUser.user;

public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final JPAQueryFactory queryFactory;

    public PostCustomRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public PostDetailResponse getPostDetailInfoById(Long postId) {
        return queryFactory
                .select(Projections.constructor(
                        PostDetailResponse.class,
                        post.id,                           // 게시글 ID
                        post.title,                        // 제목
                        post.content,                      // 내용
                        post.createdAt,                    // 생성일시
                        post.viewCount,                    // 조회수
                        JPAExpressions.select(likes.id.count().intValue())
                                .from(likes)
                                .where(likes.post.id.eq(postId)),
                        JPAExpressions.select(comment.id.count().intValue())
                                .from(comment)
                                .where(comment.post.id.eq(postId)),
                        Projections.constructor(
                                PostDetailResponse.AuthorInfo.class,
                                post.user.nickname,              // 작성자 닉네임
                                post.user.profileImage           // 작성자 프로필 이미지
                        )
                ))
                .from(post)
                .where(post.id.eq(postId))
                .fetchOne();
    }

    @Override
    public List<PostSimpleInfo> getPostSimpleInfo(Long cursor, int pageSize) {
        return queryFactory
                .select(Projections.constructor(
                        PostSimpleInfo.class,
                        post.id.as("postId"),
                        post.title,
                        post.createdAt,
                        JPAExpressions.select(likes.id.count().intValue())
                                .from(likes)
                                .where(likes.post.id.eq(post.id)),
                        JPAExpressions.select(comment.id.count().intValue())
                                .from(comment)
                                .where(comment.post.id.eq(post.id)),
                        post.viewCount,
                        post.user.nickname,
                        post.user.profileImage
                ))
                .from(post)
                .leftJoin(post.user, user)
                // leftJoin likes, comment 조인은 집계에 영향을 주지 않도록 별도 groupBy 처리
                .leftJoin(likes).on(post.id.eq(likes.post.id))
                .leftJoin(comment).on(post.id.eq(comment.post.id))
                .groupBy(post.id, post.user.nickname, post.user.profileImage)
                .where(cursor == null ? null : post.id.lt(cursor))
                .orderBy(post.id.desc())
                .limit(pageSize)
                .fetch();
    }
}
