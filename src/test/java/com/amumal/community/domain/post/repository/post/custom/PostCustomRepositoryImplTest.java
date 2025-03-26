package com.amumal.community.domain.post.repository.post.custom;

import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import com.amumal.community.domain.post.entity.Comment;
import com.amumal.community.domain.post.entity.Likes;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostCustomRepositoryTest {

    // 테스트 상수
    private static final String USER_NICKNAME = "TestUser";
    private static final String USER_EMAIL = "test@test.com";
    private static final String USER_PASSWORD = "password";
    private static final String PROFILE_IMAGE = "http://example.com/profile.jpg";
    private static final String POST_TITLE = "Test Post";
    private static final String POST_CONTENT = "This is a test post";
    private static final String POST_IMAGE = "http://example.com/image.jpg";
    private static final String COMMENT_CONTENT = "Test Comment";
    private static final String POST1_TITLE = "Post 1";
    private static final String POST2_TITLE = "Post 2";
    private static final String POST1_CONTENT = "Content 1";
    private static final String POST2_CONTENT = "Content 2";
    private static final int VIEW_COUNT_1 = 5;
    private static final int VIEW_COUNT_2 = 8;
    private static final int PAGE_SIZE = 2;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    // 테스트용 공통 엔티티
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화 (매 테스트마다 새로운 엔티티 생성)
        testUser = User.builder()
                .nickname(USER_NICKNAME)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .profileImage(PROFILE_IMAGE)
                .build();
        entityManager.persist(testUser);
    }

    @Nested
    @DisplayName("게시글 상세 조회 테스트")
    class GetPostDetailTest {

        @Test
        @DisplayName("게시글 상세 정보 조회 시 관련 정보 모두 포함")
        void getPostDetailInfoById_returnsCompleteDetails() {
            // Given: 게시글, 댓글, 좋아요 엔티티 생성 및 저장
            Post post = Post.builder()
                    .user(testUser)
                    .title(POST_TITLE)
                    .content(POST_CONTENT)
                    .image(POST_IMAGE)
                    .viewCount(0)
                    .build();
            entityManager.persist(post);

            Comment comment = Comment.builder()
                    .post(post)
                    .user(testUser)
                    .content(COMMENT_CONTENT)
                    .build();
            entityManager.persist(comment);

            Likes like = Likes.builder()
                    .post(post)
                    .user(testUser)
                    .build();
            entityManager.persist(like);

            entityManager.flush();
            entityManager.clear();

            // When: 메서드 호출
            PostDetailResponse detailResponse = postRepository.getPostDetailInfoById(post.getId());

            // Then: 응답 데이터 검증
            assertThat(detailResponse).isNotNull();

            // 게시글 기본 정보 검증
            assertThat(detailResponse.postId()).isEqualTo(post.getId());
            assertThat(detailResponse.title()).isEqualTo(POST_TITLE);
            assertThat(detailResponse.content()).isEqualTo(POST_CONTENT);
            assertThat(detailResponse.image()).isEqualTo(POST_IMAGE);
            assertThat(detailResponse.viewCount()).isEqualTo(0);

            // 좋아요, 댓글 개수 검증
            assertThat(detailResponse.likeCount()).isEqualTo(1);
            assertThat(detailResponse.commentCount()).isEqualTo(1);

            // 작성자 정보 검증
            assertThat(detailResponse.author().nickname()).isEqualTo(USER_NICKNAME);
            assertThat(detailResponse.author().profileImage()).isEqualTo(PROFILE_IMAGE);

            // 댓글 목록 검증
            assertThat(detailResponse.comments()).hasSize(1);
            assertThat(detailResponse.comments().get(0).content()).isEqualTo(COMMENT_CONTENT);
        }
    }

    @Nested
    @DisplayName("게시글 목록 조회 테스트")
    class GetPostListTest {

        @Test
        @DisplayName("게시글 목록 조회 시 최신순으로 반환")
        void getPostSimpleInfo_returnsOrderedByIdDesc() {
            // Given: 여러 게시글 엔티티 생성 및 저장
            Post post1 = Post.builder()
                    .user(testUser)
                    .title(POST1_TITLE)
                    .content(POST1_CONTENT)
                    .image(null)
                    .viewCount(VIEW_COUNT_1)
                    .build();
            entityManager.persist(post1);

            Post post2 = Post.builder()
                    .user(testUser)
                    .title(POST2_TITLE)
                    .content(POST2_CONTENT)
                    .image(null)
                    .viewCount(VIEW_COUNT_2)
                    .build();
            entityManager.persist(post2);

            entityManager.flush();
            entityManager.clear();

            // When: 커서 없이 조회 (첫 페이지)
            List<PostSimpleInfo> simpleInfos = postRepository.getPostSimpleInfo(null, PAGE_SIZE);

            // Then: 최신 게시글(ID가 큰 순)이 먼저 조회되어야 함
            assertThat(simpleInfos).hasSize(2);

            PostSimpleInfo first = simpleInfos.get(0);
            PostSimpleInfo second = simpleInfos.get(1);

            // post2가 더 나중에 생성되었으므로 ID가 더 크고 첫 번째로 와야 함
            assertThat(first.title()).isEqualTo(POST2_TITLE);
            assertThat(second.title()).isEqualTo(POST1_TITLE);
        }

        @Test
        @DisplayName("커서 기반 페이징 동작 확인")
        void getPostSimpleInfo_withCursor_returnsCursorBasedPage() {
            // Given: 여러 게시글 엔티티 생성 및 저장
            for (int i = 1; i <= 5; i++) {
                Post post = Post.builder()
                        .user(testUser)
                        .title("Post " + i)
                        .content("Content " + i)
                        .viewCount(i)
                        .build();
                entityManager.persist(post);
            }

            entityManager.flush();
            entityManager.clear();

            // When: 첫 페이지 조회 (페이지 사이즈 2)
            List<PostSimpleInfo> firstPage = postRepository.getPostSimpleInfo(null, 2);

            // 첫 페이지의 마지막 아이템 ID를 커서로 사용하여 다음 페이지 조회
            Long cursor = firstPage.get(firstPage.size() - 1).postId();
            List<PostSimpleInfo> secondPage = postRepository.getPostSimpleInfo(cursor, 2);

            // Then
            assertThat(firstPage).hasSize(2);
            assertThat(secondPage).hasSize(2);

            // 첫 페이지의 모든 게시글 ID는 두 번째 페이지의 모든 게시글 ID보다 커야 함
            Long minIdInFirstPage = firstPage.stream()
                    .map(PostSimpleInfo::postId)
                    .min(Long::compareTo)
                    .orElse(0L);

            Long maxIdInSecondPage = secondPage.stream()
                    .map(PostSimpleInfo::postId)
                    .max(Long::compareTo)
                    .orElse(0L);

            assertThat(minIdInFirstPage).isGreaterThan(maxIdInSecondPage);
        }
    }
}