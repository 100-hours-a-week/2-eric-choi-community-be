package com.amumal.community.domain.post.service.post.impl;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.post.PostRepository;
import com.amumal.community.domain.post.service.post.PostCommandService;
import com.amumal.community.global.exception.CustomException;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandServiceImpl implements PostCommandService {

    private final PostRepository postRepository;

    @Override
    public Long createPost(PostRequest request, User currentUser) {
        Post post = Post.builder()
                .user(currentUser)
                .title(request.title())
                .content(request.content())
                .image(request.image())
                .viewCount(0)
                .build();
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    @Override
    public void updatePost(Long postId, PostRequest request, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
        post.update(request, currentUser);
    }

    @Override
    public void deletePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.NOT_FOUND));
        post.safeDelete(currentUser);
    }
}