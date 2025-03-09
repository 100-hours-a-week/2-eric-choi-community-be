package com.amumal.community.domain.post.service;

import com.amumal.community.domain.post.dto.request.PostRequestDto;
import com.amumal.community.domain.post.dto.response.PostResponseDto;
import com.amumal.community.domain.post.entity.Post;
import com.amumal.community.domain.post.repository.PostRepository;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // 게시글 생성
    public PostResponseDto createPost(PostRequestDto postRequestDto) {
        User user = userRepository.findById(postRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setTitle(postRequestDto.getTitle());
        post.setContent(postRequestDto.getContent());
        post.setAuthor(user);
        post.setLikeCount(0);  // 초기 좋아요 수
        post.setCommentCount(0);  // 초기 댓글 수
        post.setViewCount(0);  // 초기 조회 수

        postRepository.save(post);

        return new PostResponseDto(post.getId(), post.getTitle(), post.getContent(),
                post.getLikeCount(), post.getCommentCount(), post.getViewCount(),
                post.getAuthor().getNickname(), post.getAuthor().getProfileImage(), post.getCreatedAt(), post.getUpdatedAt());
    }

    // 게시글 목록 조회
    public List<PostResponseDto> getPosts() {
        List<Post> posts = postRepository.findAllByDeletedAtIsNull();
        return posts.stream()
                .map(post -> new PostResponseDto(post.getId(), post.getTitle(), post.getContent(),
                        post.getLikeCount(), post.getCommentCount(), post.getViewCount(),
                        post.getAuthor().getNickname(), post.getAuthor().getProfileImage(), post.getCreatedAt(), post.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    public PostResponseDto getPostById(Long id) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(id);
        return new PostResponseDto(post.getId(), post.getTitle(), post.getContent(),
                post.getLikeCount(), post.getCommentCount(), post.getViewCount(),
                post.getAuthor().getNickname(), post.getAuthor().getProfileImage(), post.getCreatedAt(), post.getUpdatedAt());
    }
}
