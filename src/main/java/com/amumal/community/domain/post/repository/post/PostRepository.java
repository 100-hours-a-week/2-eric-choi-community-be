package com.amumal.community.domain.post.repository.post;

import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.custom.PostCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostCustomRepository {
    // 기본 CRUD와 커스텀 쿼리 메서드를 함께 사용할 수 있습니다.
}
