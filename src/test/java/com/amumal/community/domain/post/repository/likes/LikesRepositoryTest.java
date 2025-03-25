package com.amumal.community.domain.post.repository.likes;

import com.amumal.community.domain.post.entity.Likes;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LikesRepositoryTest {

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("존재하는 좋아요 여부 조회")
    void existsByPostIdAndUserId_true() {
        // Given: User, Post, Likes 엔티티 생성 후 저장
        User user = User.builder()
                .nickname("TestUser")
                .email("test@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        Post post = Post.builder()
                .user(user)
                .title("Test Post")
                .content("Test Content")
                .build();
        entityManager.persist(post);

        Likes like = Likes.builder()
                .post(post)
                .user(user)
                .build();
        entityManager.persist(like);
        entityManager.flush();
        entityManager.clear();

        // When: 존재 여부 확인
        boolean exists = likesRepository.existsByPostIdAndUserId(post.getId(), user.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 좋아요 여부 조회")
    void existsByPostIdAndUserId_false() {
        // Given: User, Post 엔티티 저장 (Likes는 저장하지 않음)
        User user = User.builder()
                .nickname("TestUser")
                .email("test@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        Post post = Post.builder()
                .user(user)
                .title("Test Post")
                .content("Test Content")
                .build();
        entityManager.persist(post);
        entityManager.flush();
        entityManager.clear();

        // When: 존재 여부 확인
        boolean exists = likesRepository.existsByPostIdAndUserId(post.getId(), user.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByPostIdAndUserId - 좋아요 존재하면 Optional 반환")
    void findByPostIdAndUserId_exists() {
        // Given
        User user = User.builder()
                .nickname("TestUser")
                .email("test@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        Post post = Post.builder()
                .user(user)
                .title("Test Post")
                .content("Test Content")
                .build();
        entityManager.persist(post);

        Likes like = Likes.builder()
                .post(post)
                .user(user)
                .build();
        entityManager.persist(like);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Likes> optionalLike = likesRepository.findByPostIdAndUserId(post.getId(), user.getId());

        // Then
        assertThat(optionalLike).isPresent();
        // 엔티티의 관계가 올바르게 매핑되었는지 추가 검증
        assertThat(optionalLike.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(optionalLike.get().getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("findByPostIdAndUserId - 좋아요가 없으면 빈 Optional 반환")
    void findByPostIdAndUserId_notExists() {
        // Given
        User user = User.builder()
                .nickname("TestUser")
                .email("test@example.com")
                .password("password")
                .build();
        entityManager.persist(user);

        Post post = Post.builder()
                .user(user)
                .title("Test Post")
                .content("Test Content")
                .build();
        entityManager.persist(post);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Likes> optionalLike = likesRepository.findByPostIdAndUserId(post.getId(), user.getId());

        // Then
        assertThat(optionalLike).isNotPresent();
    }
}
