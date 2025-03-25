package com.amumal.community.domain.post.repository.post.custom;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import com.amumal.community.domain.post.entity.Comment;
import com.amumal.community.domain.post.entity.Likes;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostCustomRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("게시글 상세 정보 조회 테스트")
    void getPostDetailInfoById_test() {
        // 데이터 준비
        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .profileImage("http://example.com/profile.jpg")
                .build();
        entityManager.persist(user);

        Post post = Post.builder()
                .user(user)
                .title("Test Post")
                .content("This is a test post")
                .image("http://example.com/image.jpg")
                .viewCount(0)
                .build();
        entityManager.persist(post);

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content("Test Comment")
                .build();
        entityManager.persist(comment);

        Likes like = Likes.builder()
                .post(post)
                .user(user)
                .build();
        entityManager.persist(like);

        entityManager.flush();
        entityManager.clear();

        // 메서드 호출
        PostDetailResponse detailResponse = postRepository.getPostDetailInfoById(post.getId());

        // 검증
        assertThat(detailResponse).isNotNull();
        assertThat(detailResponse.postId()).isEqualTo(post.getId());
        assertThat(detailResponse.title()).isEqualTo("Test Post");
        assertThat(detailResponse.content()).isEqualTo("This is a test post");
        assertThat(detailResponse.image()).isEqualTo("http://example.com/image.jpg");
        assertThat(detailResponse.viewCount()).isEqualTo(0);

        // 좋아요와 댓글 개수는 각각 1로 예상
        assertThat(detailResponse.likeCount()).isEqualTo(1);
        assertThat(detailResponse.commentCount()).isEqualTo(1);
        // 작성자 정보 검증
        assertThat(detailResponse.author().nickname()).isEqualTo("TestUser");
        assertThat(detailResponse.author().profileImage()).isEqualTo("http://example.com/profile.jpg");
        // 댓글 목록 검증
        assertThat(detailResponse.comments()).hasSize(1);
        assertThat(detailResponse.comments().get(0).content()).isEqualTo("Test Comment");
    }

    @Test
    @DisplayName("게시글 단순 정보 조회 테스트")
    void getPostSimpleInfo_test() {
        // 데이터 준비
        User user = User.builder()
                .nickname("TestUser")
                .email("test@test.com")
                .password("password")
                .profileImage("http://example.com/profile.jpg")
                .build();
        entityManager.persist(user);

        Post post1 = Post.builder()
                .user(user)
                .title("Post 1")
                .content("Content 1")
                .image(null)
                .viewCount(5)
                .build();
        Post post2 = Post.builder()
                .user(user)
                .title("Post 2")
                .content("Content 2")
                .image(null)
                .viewCount(8)
                .build();
        entityManager.persist(post1);
        entityManager.persist(post2);

        entityManager.flush();
        entityManager.clear();

        // 커서를 null로 지정하면 id 내림차순 정렬로 모두 조회 (pageSize 2)
        List<PostSimpleInfo> simpleInfos = postRepository.getPostSimpleInfo(null, 2);

        // 검증: 최신 게시글이 첫 번째로 조회됨 (post2가 post1보다 나중에 생성되었다고 가정)
        assertThat(simpleInfos).hasSize(2);
        PostSimpleInfo first = simpleInfos.get(0);
        PostSimpleInfo second = simpleInfos.get(1);
        assertThat(first.title()).isEqualTo("Post 2");
        assertThat(second.title()).isEqualTo("Post 1");
    }
}
