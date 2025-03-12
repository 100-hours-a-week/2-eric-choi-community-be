package com.amumal.community.domain.post.repository.likes;

import com.amumal.community.domain.post.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    Optional<Likes> findByPostIdAndUserId(Long postId, Long userId);
}
