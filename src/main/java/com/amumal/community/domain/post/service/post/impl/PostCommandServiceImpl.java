package com.amumal.community.domain.post.service.post.impl;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.post.service.post.PostCommandService;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.global.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandServiceImpl implements PostCommandService {

    private final PostRepository postRepository;
    private final S3Service s3Service;

    @Override
    public Long createPost(PostRequest request, MultipartFile image, User currentUser) {
        // 이미지가 있으면 S3에 업로드
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = s3Service.uploadImage(image);
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 중 오류 발생", e);
            }
        }

        Post post = Post.builder()
                .user(currentUser)
                .title(request.title())
                .content(request.content())
                .image(imageUrl) // S3 이미지 URL 저장
                .viewCount(0)
                .build();
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    @Override
    public void updatePost(Long postId, PostRequest request, MultipartFile image, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));

        // 이미지 처리
        String imageUrl = post.getImage(); // 기존 이미지 URL

        // 새 이미지가 업로드된 경우
        if (image != null && !image.isEmpty()) {
            // 기존 이미지가 있으면 삭제
            if (imageUrl != null && s3Service.isValidS3Url(imageUrl)) {
                s3Service.deleteImage(imageUrl);
            }

            // 새 이미지 업로드
            try {
                imageUrl = s3Service.uploadImage(image);
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 중 오류 발생", e);
            }
        }

        // PostRequest의 원래 구조를 유지하면서 새로운 이미지 URL을 가진 객체 생성
        PostRequest updatedRequest = PostRequest.builder()
                .title(request.title())
                .content(request.content())
                .image(imageUrl)
                .build();

        post.update(updatedRequest, currentUser);
    }

    @Override
    public void deletePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));

        // 게시글에 연결된 이미지가 있으면 S3에서 삭제
        String imageUrl = post.getImage();
        if (imageUrl != null && s3Service.isValidS3Url(imageUrl)) {
            s3Service.deleteImage(imageUrl);
        }

        post.safeDelete(currentUser);
    }
}