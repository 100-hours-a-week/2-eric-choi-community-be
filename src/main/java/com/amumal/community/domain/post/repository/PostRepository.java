package com.amumal.community.domain.post.repository;

import com.amumal.community.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시글 ID로 조회
    Post findByIdAndDeletedAtIsNull(Long id);

    // 모든 게시글 조회 (삭제되지 않은 게시글)
    List<Post> findAllByDeletedAtIsNull();
}
