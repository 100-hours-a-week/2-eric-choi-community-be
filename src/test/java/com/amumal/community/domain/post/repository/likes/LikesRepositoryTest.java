package com.amumal.community.domain.post.repository.likes;

import com.amumal.community.domain.post.entity.Likes;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LikesRepositoryTest {

    // 테스트 상수
    private static final String USER_NICKNAME = "TestUser";
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_PASSWORD = "password";
    private static final String POST_TITLE = "Test Post";
    private static final String POST_CONTENT = "Test Content";

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private TestEntityManager entityManager;

    // 테스트용 공통 엔티티
    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화 (매 테스트마다 새로운 엔티티 생성)
        testUser = User.builder()
                .nickname(USER_NICKNAME)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .build();
        entityManager.persist(testUser);

        testPost = Post.builder()
                .user(testUser)
                .title(POST_TITLE)
                .content(POST_CONTENT)
                .build();
        entityManager.persist(testPost);
    }

    @Nested
    @DisplayName("좋아요 존재 여부 조회 테스트")
    class ExistsByPostIdAndUserIdTest {

        @Test
        @DisplayName("좋아요가 존재하는 경우 true 반환")
        void existsByPostIdAndUserId_likeExists_returnsTrue() {
            // Given: 좋아요 엔티티 생성 후 저장
            Likes like = Likes.builder()
                    .post(testPost)
                    .user(testUser)
                    .build();
            entityManager.persist(like);
            entityManager.flush();
            entityManager.clear();

            // When
            boolean exists = likesRepository.existsByPostIdAndUserId(testPost.getId(), testUser.getId());

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("좋아요가 존재하지 않는 경우 false 반환")
        void existsByPostIdAndUserId_likeNotExists_returnsFalse() {
            // Given: 좋아요 엔티티 저장하지 않음
            entityManager.flush();
            entityManager.clear();

            // When
            boolean exists = likesRepository.existsByPostIdAndUserId(testPost.getId(), testUser.getId());

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("좋아요 조회 테스트")
    class FindByPostIdAndUserIdTest {

        @Test
        @DisplayName("좋아요가 존재하는 경우 Optional에 담아 반환")
        void findByPostIdAndUserId_likeExists_returnsOptionalWithLike() {
            // Given
            Likes like = Likes.builder()
                    .post(testPost)
                    .user(testUser)
                    .build();
            entityManager.persist(like);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Likes> optionalLike = likesRepository.findByPostIdAndUserId(testPost.getId(), testUser.getId());

            // Then
            assertThat(optionalLike).isPresent();
            // 엔티티의 관계가 올바르게 매핑되었는지 추가 검증
            assertThat(optionalLike.get().getUser().getId()).isEqualTo(testUser.getId());
            assertThat(optionalLike.get().getPost().getId()).isEqualTo(testPost.getId());
        }

        @Test
        @DisplayName("좋아요가 존재하지 않는 경우 빈 Optional 반환")
        void findByPostIdAndUserId_likeNotExists_returnsEmptyOptional() {
            // Given: 좋아요 엔티티 저장하지 않음
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Likes> optionalLike = likesRepository.findByPostIdAndUserId(testPost.getId(), testUser.getId());

            // Then
            assertThat(optionalLike).isEmpty();
        }
    }
}