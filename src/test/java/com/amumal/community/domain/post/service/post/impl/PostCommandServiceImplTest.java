package com.amumal.community.domain.post.service.post.impl;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.post.service.post.PostCommandService;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.s3.service.S3Service;
import com.amumal.community.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private PostRepository postRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PostCommandServiceImpl postCommandService;

    @Test
    @DisplayName("이미지 없이 게시글 생성")
    void createPost_noImage_success() throws IOException {
        // Given
        PostRequest req = mock(PostRequest.class);
        when(req.title()).thenReturn("제목");
        when(req.content()).thenReturn("내용");
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(true);
        User user = mock(User.class);
        Post savedPost = spy(Post.builder()
                .user(user)
                .title("제목")
                .content("내용")
                .image(null)
                .viewCount(0)
                .build());
        when(savedPost.getId()).thenReturn(1L);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        // When
        Long resultId = postCommandService.createPost(req, image, user);
        // Then
        assertEquals(1L, resultId);
        verify(s3Service, never()).uploadImage(any());
    }

    @Test
    @DisplayName("이미지와 함께 게시글 생성")
    void createPost_withImage_success() throws IOException {
        // Given
        PostRequest req = mock(PostRequest.class);
        when(req.title()).thenReturn("제목");
        when(req.content()).thenReturn("내용");
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        User user = mock(User.class);

        String imageUrl = "http://s3.aws.com/image.jpg";
        when(s3Service.uploadImage(image)).thenReturn(imageUrl);
        Post savedPost = spy(Post.builder()
                .user(user)
                .title("제목")
                .content("내용")
                .image(imageUrl)
                .viewCount(0)
                .build());
        when(savedPost.getId()).thenReturn(2L);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        // When
        Long resultId = postCommandService.createPost(req, image, user);
        // Then
        assertEquals(2L, resultId);
        verify(s3Service, times(1)).uploadImage(image);
    }

    @Test
    @DisplayName("이미지 업로드 실패 시 게시글 생성 예외 발생")
    void createPost_withImageUploadFailure_exception() throws IOException {
        // Given
        PostRequest req = mock(PostRequest.class);
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        User user = mock(User.class);
        when(s3Service.uploadImage(image)).thenThrow(new IOException("S3 error"));
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                postCommandService.createPost(req, image, user));
        assertTrue(exception.getMessage().contains("이미지 업로드 중 오류 발생"));
    }

    @Test
    @DisplayName("이미지 변경 없이 게시글 수정")
    void updatePost_noImageChange_success() throws IOException {
        // Given
        Long postId = 1L;
        PostRequest req = mock(PostRequest.class);
        when(req.title()).thenReturn("새 제목");
        when(req.content()).thenReturn("새 내용");
        MultipartFile image = null;
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Post post = spy(Post.builder()
                .user(user)
                .title("옛 제목")
                .content("옛 내용")
                .image("기존 이미지")
                .viewCount(0)
                .build());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        // When
        postCommandService.updatePost(postId, req, image, user);
        // Then
        verify(s3Service, never()).uploadImage(any());
        verify(s3Service, never()).deleteImage(any());
        verify(post).updateContent("새 제목", "새 내용", "기존 이미지");
    }

    @Test
    @DisplayName("이미지 변경과 함께 게시글 수정")
    void updatePost_withImageChange_success() throws IOException {
        // Given
        Long postId = 1L;
        PostRequest req = mock(PostRequest.class);
        when(req.title()).thenReturn("새 제목");
        when(req.content()).thenReturn("새 내용");
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        String oldImage = "http://s3.aws.com/old.jpg";
        Post post = spy(Post.builder()
                .user(user)
                .title("옛 제목")
                .content("옛 내용")
                .image(oldImage)
                .viewCount(0)
                .build());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(s3Service.isValidS3Url(oldImage)).thenReturn(true);
        String newImage = "http://s3.aws.com/new.jpg";
        when(s3Service.uploadImage(image)).thenReturn(newImage);
        // When
        postCommandService.updatePost(postId, req, image, user);
        // Then
        verify(s3Service).isValidS3Url(oldImage);
        verify(s3Service).deleteImage(oldImage);
        verify(s3Service).uploadImage(image);
        verify(post).updateContent("새 제목", "새 내용", newImage);
    }

    @Test
    @DisplayName("이미지 업로드 실패 시 게시글 수정 예외 발생")
    void updatePost_withImageUploadFailure_exception() throws IOException {
        // Given
        Long postId = 1L;
        PostRequest req = mock(PostRequest.class);
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        String oldImage = "http://s3.aws.com/old.jpg";
        Post post = spy(Post.builder()
                .user(user)
                .title("옛 제목")
                .content("옛 내용")
                .image(oldImage)
                .viewCount(0)
                .build());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(s3Service.isValidS3Url(oldImage)).thenReturn(true);
        when(s3Service.uploadImage(image)).thenThrow(new IOException("S3 error"));
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                postCommandService.updatePost(postId, req, image, user));
        assertTrue(exception.getMessage().contains("이미지 업로드 중 오류 발생"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
    void updatePost_postNotFound_exception() {
        // Given
        Long postId = 1L;
        PostRequest req = mock(PostRequest.class);
        MultipartFile image = null;
        User user = mock(User.class);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postCommandService.updatePost(postId, req, image, user));
        assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    @DisplayName("게시글 소유권이 없는 경우 수정 시 예외 발생")
    void updatePost_unauthorized_exception() {
        // Given
        Long postId = 1L;
        PostRequest req = mock(PostRequest.class);
        MultipartFile image = null;
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(1L);
        User other = mock(User.class);
        when(other.getId()).thenReturn(2L);
        Post post = spy(Post.builder()
                .user(owner)
                .title("옛 제목")
                .content("옛 내용")
                .image("이미지")
                .viewCount(0)
                .build());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postCommandService.updatePost(postId, req, image, other));
        assertEquals(CustomResponseStatus.UNAUTHORIZED_REQUEST, exception.getStatus());
    }

    @Test
    @DisplayName("유효한 이미지가 있는 게시글 삭제")
    void deletePost_withValidImage_success() {
        // Given
        Long postId = 1L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        String imageUrl = "http://s3.aws.com/image.jpg";
        Post post = spy(Post.builder()
                .user(user)
                .title("제목")
                .content("내용")
                .image(imageUrl)
                .viewCount(0)
                .build());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(s3Service.isValidS3Url(imageUrl)).thenReturn(true);
        // When
        postCommandService.deletePost(postId, user);
        // Then
        verify(s3Service).isValidS3Url(imageUrl);
        verify(s3Service).deleteImage(imageUrl);
        verify(post).delete();
    }

    @Test
    @DisplayName("게시글 삭제 시 이미지가 없으면 삭제")
    void deletePost_noImage_success() {
        // Given
        Long postId = 1L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        Post post = spy(Post.builder()
                .user(user)
                .title("제목")
                .content("내용")
                .image(null)
                .viewCount(0)
                .build());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        // When
        postCommandService.deletePost(postId, user);
        // Then
        verify(s3Service, never()).deleteImage(any());
        verify(post).delete();
    }

    @Test
    @DisplayName("게시글 삭제 시 이미지가 유효하지 않으면 삭제")
    void deletePost_invalidImage_success() {
        // Given
        Long postId = 1L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        String imageUrl = "invalid_url";
        Post post = spy(Post.builder()
                .user(user)
                .title("제목")
                .content("내용")
                .image(imageUrl)
                .viewCount(0)
                .build());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(s3Service.isValidS3Url(imageUrl)).thenReturn(false);
        // When
        postCommandService.deletePost(postId, user);
        // Then
        verify(s3Service).isValidS3Url(imageUrl);
        verify(s3Service, never()).deleteImage(any());
        verify(post).delete();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
    void deletePost_postNotFound_exception() {
        // Given
        Long postId = 1L;
        User user = mock(User.class);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postCommandService.deletePost(postId, user));
        assertEquals(CustomResponseStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    @DisplayName("게시글 소유권이 없는 경우 삭제 시 예외 발생")
    void deletePost_unauthorized_exception() {
        // Given
        Long postId = 1L;
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(1L);
        User other = mock(User.class);
        when(other.getId()).thenReturn(2L);
        Post post = spy(Post.builder()
                .user(owner)
                .title("제목")
                .content("내용")
                .image(null)
                .viewCount(0)
                .build());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                postCommandService.deletePost(postId, other));
        assertEquals(CustomResponseStatus.UNAUTHORIZED_REQUEST, exception.getStatus());
    }
}
