package com.amumal.community.domain.post.repository.comment;

import com.amumal.community.domain.post.entity.Comment;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    // 테스트 상수
    private static final String USER_NICKNAME = "TestUser";
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_PASSWORD = "password";
    private static final String POST_TITLE = "Test Post";
    private static final String POST_CONTENT = "Test Content";
    private static final String COMMENT_CONTENT = "Test Comment";

    @Autowired
    private CommentRepository commentRepository;

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

    @Test
    @DisplayName("댓글 저장 및 ID로 조회 성공")
    void save_andFindById_returnsComment() {
        // Given
        Comment comment = Comment.builder()
                .post(testPost)
                .user(testUser)
                .content(COMMENT_CONTENT)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // DB에 반영하고 영속성 컨텍스트를 초기화하여 실제 DB에서 조회
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());

        // Then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getContent()).isEqualTo(COMMENT_CONTENT);
        assertThat(foundComment.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(foundComment.get().getPost().getId()).isEqualTo(testPost.getId());
    }
}