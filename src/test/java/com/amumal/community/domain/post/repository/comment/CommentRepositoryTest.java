package com.amumal.community.domain.post.repository.comment;

import com.amumal.community.domain.post.entity.Comment;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("댓글 저장 및 조회 테스트")
    void saveAndFindById() {
        // Given
        // 필요한 엔티티(User, Post)를 미리 저장합니다.
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

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content("Test Comment")
                .build();

        Comment savedComment = commentRepository.save(comment);
        // DB에 반영하고 영속성 컨텍스트를 초기화하여 실제 DB에서 조회하도록 합니다.
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());

        // Then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getContent()).isEqualTo("Test Comment");
    }
}
