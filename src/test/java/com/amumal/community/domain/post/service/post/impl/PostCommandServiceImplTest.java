package com.amumal.community.domain.post.service.post.impl;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.s3.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceImplTest {

    // 테스트 상수
    private static final Long POST_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final String TITLE = "제목";
    private static final String CONTENT = "내용";
    private static final String NEW_TITLE = "새 제목";
    private static final String NEW_CONTENT = "새 내용";
    private static final String IMAGE_URL = "http://s3.aws.com/image.jpg";
    private static final String OLD_IMAGE_URL = "http://s3.aws.com/old.jpg";
    private static final String NEW_IMAGE_URL = "http://s3.aws.com/new.jpg";

    @Mock
    private PostRepository postRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PostCommandServiceImpl postCommandService;

    @Nested
    @DisplayName("게시글 생성 테스트")
    class CreatePostTest {

        @Test
        @DisplayName("이미지 없이 게시글 생성")
        void createPost_noImage_success() throws IOException {
            // Given
            User user = mock(User.class);
            PostRequest req = mock(PostRequest.class);
            when(req.title()).thenReturn(TITLE);
            when(req.content()).thenReturn(CONTENT);

            MultipartFile image = mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(true);

            Post savedPost = mock(Post.class);
            when(savedPost.getId()).thenReturn(POST_ID);
            when(postRepository.save(any(Post.class))).thenReturn(savedPost);

            // When
            Long resultId = postCommandService.createPost(req, image, user);

            // Then
            assertEquals(POST_ID, resultId);
            verify(postRepository).save(any(Post.class));
            verify(s3Service, never()).uploadImage(any());
        }

        @Test
        @DisplayName("이미지와 함께 게시글 생성")
        void createPost_withImage_success() throws IOException {
            // Given
            User user = mock(User.class);
            PostRequest req = mock(PostRequest.class);
            when(req.title()).thenReturn(TITLE);
            when(req.content()).thenReturn(CONTENT);

            MultipartFile image = mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(false);

            when(s3Service.uploadImage(image)).thenReturn(IMAGE_URL);

            Post savedPost = mock(Post.class);
            when(savedPost.getId()).thenReturn(POST_ID);
            when(postRepository.save(any(Post.class))).thenReturn(savedPost);

            // When
            Long resultId = postCommandService.createPost(req, image, user);

            // Then
            assertEquals(POST_ID, resultId);
            verify(s3Service).uploadImage(image);
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("이미지 업로드 실패 시 게시글 생성 예외 발생")
        void createPost_withImageUploadFailure_exception() throws IOException {
            // Given
            User user = mock(User.class);
            PostRequest req = mock(PostRequest.class);

            MultipartFile image = mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(false);

            when(s3Service.uploadImage(image)).thenThrow(new IOException("S3 error"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postCommandService.createPost(req, image, user));
            assertTrue(exception.getMessage().contains("이미지 업로드 중 오류 발생"));
        }
    }

    @Nested
    @DisplayName("게시글 수정 테스트")
    class UpdatePostTest {

        @Test
        @DisplayName("이미지 변경 없이 게시글 수정")
        void updatePost_noImageChange_success() throws IOException {
            // Given
            User user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);

            PostRequest req = mock(PostRequest.class);
            when(req.title()).thenReturn(NEW_TITLE);
            when(req.content()).thenReturn(NEW_CONTENT);

            Post post = mock(Post.class);
            when(post.getUser()).thenReturn(user);
            when(post.getImage()).thenReturn(IMAGE_URL);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

            // When
            postCommandService.updatePost(POST_ID, req, null, user);

            // Then
            verify(post).updateContent(NEW_TITLE, NEW_CONTENT, IMAGE_URL);
            verify(s3Service, never()).uploadImage(any());
            verify(s3Service, never()).deleteImage(any());
        }

        @Test
        @DisplayName("이미지 변경과 함께 게시글 수정")
        void updatePost_withImageChange_success() throws IOException {
            // Given
            User user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);

            PostRequest req = mock(PostRequest.class);
            when(req.title()).thenReturn(NEW_TITLE);
            when(req.content()).thenReturn(NEW_CONTENT);

            MultipartFile image = mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(false);

            Post post = mock(Post.class);
            when(post.getUser()).thenReturn(user);
            when(post.getImage()).thenReturn(OLD_IMAGE_URL);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

            when(s3Service.isValidS3Url(OLD_IMAGE_URL)).thenReturn(true);
            when(s3Service.uploadImage(image)).thenReturn(NEW_IMAGE_URL);

            // When
            postCommandService.updatePost(POST_ID, req, image, user);

            // Then
            verify(s3Service).isValidS3Url(OLD_IMAGE_URL);
            verify(s3Service).deleteImage(OLD_IMAGE_URL);
            verify(s3Service).uploadImage(image);
            verify(post).updateContent(NEW_TITLE, NEW_CONTENT, NEW_IMAGE_URL);
        }

        @Test
        @DisplayName("이미지 업로드 실패 시 게시글 수정 예외 발생")
        void updatePost_withImageUploadFailure_exception() throws IOException {
            // Given
            User user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);

            PostRequest req = mock(PostRequest.class);

            MultipartFile image = mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(false);

            Post post = mock(Post.class);
            when(post.getUser()).thenReturn(user);
            when(post.getImage()).thenReturn(OLD_IMAGE_URL);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

            when(s3Service.isValidS3Url(OLD_IMAGE_URL)).thenReturn(true);
            when(s3Service.uploadImage(image)).thenThrow(new IOException("S3 error"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postCommandService.updatePost(POST_ID, req, image, user));
            assertTrue(exception.getMessage().contains("이미지 업로드 중 오류 발생"));
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
        void updatePost_postNotFound_exception() {
            // Given
            User user = mock(User.class);
            PostRequest req = mock(PostRequest.class);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> postCommandService.updatePost(POST_ID, req, null, user));
            assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
        }

        @Test
        @DisplayName("게시글 소유권이 없는 경우 수정 시 예외 발생")
        void updatePost_unauthorized_exception() {
            // Given
            User owner = mock(User.class);
            when(owner.getId()).thenReturn(USER_ID);

            User other = mock(User.class);
            when(other.getId()).thenReturn(OTHER_USER_ID);

            Post post = mock(Post.class);
            when(post.getUser()).thenReturn(owner);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

            PostRequest req = mock(PostRequest.class);

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> postCommandService.updatePost(POST_ID, req, null, other));
            assertEquals(CustomResponseStatus.UNAUTHORIZED_REQUEST, exception.getStatus());
        }
    }

    @Nested
    @DisplayName("게시글 삭제 테스트")
    class DeletePostTest {

        @Test
        @DisplayName("유효한 이미지가 있는 게시글 삭제")
        void deletePost_withValidImage_success() {
            // Given
            User user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);

            Post post = mock(Post.class);
            when(post.getUser()).thenReturn(user);
            when(post.getImage()).thenReturn(IMAGE_URL);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

            when(s3Service.isValidS3Url(IMAGE_URL)).thenReturn(true);

            // When
            postCommandService.deletePost(POST_ID, user);

            // Then
            verify(s3Service).isValidS3Url(IMAGE_URL);
            verify(s3Service).deleteImage(IMAGE_URL);
            verify(post).delete();
        }

        @Test
        @DisplayName("게시글 삭제 시 이미지가 없으면 삭제")
        void deletePost_noImage_success() {
            // Given
            User user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);

            Post post = mock(Post.class);
            when(post.getUser()).thenReturn(user);
            when(post.getImage()).thenReturn(null);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

            // When
            postCommandService.deletePost(POST_ID, user);

            // Then
            verify(s3Service, never()).deleteImage(any());
            verify(post).delete();
        }

        @Test
        @DisplayName("게시글 삭제 시 이미지가 유효하지 않으면 삭제")
        void deletePost_invalidImage_success() {
            // Given
            User user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);

            String invalidImageUrl = "invalid_url";
            Post post = mock(Post.class);
            when(post.getUser()).thenReturn(user);
            when(post.getImage()).thenReturn(invalidImageUrl);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

            when(s3Service.isValidS3Url(invalidImageUrl)).thenReturn(false);

            // When
            postCommandService.deletePost(POST_ID, user);

            // Then
            verify(s3Service).isValidS3Url(invalidImageUrl);
            verify(s3Service, never()).deleteImage(any());
            verify(post).delete();
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
        void deletePost_postNotFound_exception() {
            // Given
            User user = mock(User.class);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> postCommandService.deletePost(POST_ID, user));
            assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
        }

        @Test
        @DisplayName("게시글 소유권이 없는 경우 삭제 시 예외 발생")
        void deletePost_unauthorized_exception() {
            // Given
            User owner = mock(User.class);
            when(owner.getId()).thenReturn(USER_ID);

            User other = mock(User.class);
            when(other.getId()).thenReturn(OTHER_USER_ID);

            Post post = mock(Post.class);
            when(post.getUser()).thenReturn(owner);
            when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));

            // When & Then
            CustomException exception = assertThrows(CustomException.class,
                    () -> postCommandService.deletePost(POST_ID, other));
            assertEquals(CustomResponseStatus.UNAUTHORIZED_REQUEST, exception.getStatus());
        }
    }
}