package com.amumal.community.domain.post.repository.post.custom;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.Collections;
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
        // 1. 게시글 기본 정보 조회 (댓글 자리에는 빈 리스트를 전달)
        PostDetailResponse postDetail = queryFactory
                .select(Projections.constructor(
                        PostDetailResponse.class,
                        post.id,
                        post.title,
                        post.content,
                        post.image,
                        post.createdAt,
                        post.viewCount,
                        JPAExpressions.select(likes.id.count().intValue())
                                .from(likes)
                                .where(likes.post.id.eq(postId)),
                        JPAExpressions.select(comment.id.count().intValue())
                                .from(comment)
                                .where(
                                        comment.post.id.eq(postId),
                                        comment.deletedAt.isNull()
                                ),
                        Projections.constructor(
                                PostDetailResponse.AuthorInfo.class,
                                post.user.nickname,
                                post.user.profileImage
                        ),
                        Expressions.constant(Collections.emptyList())
                ))
                .from(post)
                .where(post.id.eq(postId))
                .fetchOne();

        if (postDetail == null) {
            return null;
        }

        // 2. 해당 게시글의 댓글 목록 조회
        List<PostDetailResponse.CommentResponse> comments = queryFactory
                .select(Projections.constructor(
                        PostDetailResponse.CommentResponse.class,
                        comment.id,
                        comment.content,
                        comment.createdAt,
                        Projections.constructor(
                                PostDetailResponse.AuthorInfo.class,
                                comment.user.nickname,
                                comment.user.profileImage
                        )
                ))
                .from(comment)
                .leftJoin(comment.user, user)
                .where(
                        comment.post.id.eq(postId),
                        comment.deletedAt.isNull()   // soft delete된 댓글은 제외
                )
                .fetch();

        // 3. 조회된 댓글 목록을 포함하여 PostDetailResponse 객체 재구성 (빌더 사용)
        return PostDetailResponse.builder()
                .postId(postDetail.postId())
                .title(postDetail.title())
                .content(postDetail.content())
                .image(postDetail.image())
                .createdAt(postDetail.createdAt())
                .viewCount(postDetail.viewCount())
                .likeCount(postDetail.likeCount())
                .commentCount(postDetail.commentCount())
                .author(postDetail.author())
                .comments(comments)
                .build();
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
                                .where(
                                        comment.post.id.eq(post.id),
                                        comment.deletedAt.isNull()),
                        post.viewCount,
                        post.user.nickname,
                        post.user.profileImage
                ))
                .from(post)
                .leftJoin(post.user, user)
                .leftJoin(likes).on(post.id.eq(likes.post.id))
                .leftJoin(comment).on(post.id.eq(comment.post.id))
                .groupBy(post.id, post.user.nickname, post.user.profileImage)
                .where(
                        post.deletedAt.isNull(),
                        cursor == null ? null : post.id.lt(cursor)
                )
                .orderBy(post.id.desc())
                .limit(pageSize)
                .fetch();
    }
}
