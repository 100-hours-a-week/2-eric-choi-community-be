package com.amumal.community.domain.post.repository.comment;

import com.amumal.community.domain.post.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 게시글의 댓글 목록을 조회할 수 있습니다.
    List<Comment> findByPostId(Long postId);
}
