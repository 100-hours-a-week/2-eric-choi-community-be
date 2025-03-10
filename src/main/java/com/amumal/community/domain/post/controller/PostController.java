package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.PostRequestDto;
import com.amumal.community.domain.post.dto.response.PostResponseDto;
import com.amumal.community.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public PostResponseDto createPost(@RequestBody PostRequestDto postRequestDto) {
        return postService.createPost(postRequestDto);
    }

    // 게시글 목록 조회
    @GetMapping
    public List<PostResponseDto> getPosts() {
        return postService.getPosts();
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public PostResponseDto getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }
}