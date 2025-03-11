package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.dto.response.PostResponse.PostSimpleInfo;
import com.amumal.community.domain.post.service.PostService;
import com.amumal.community.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 목록 조회 (커서 기반 페이지네이션)
     * GET /posts?cursor={cursor}&pageSize={pageSize}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PostResponse>> getPosts(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        PostResponse response = postService.getPostSimpleInfo(cursor, pageSize);
        return ResponseEntity.ok(new ApiResponse<>("fetch_posts_success", response));
    }

    /**
     * 게시글 상세 조회
     * GET /posts/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(@PathVariable Long postId) {
        PostDetailResponse response = postService.getPostDetailInfoById(postId);
        return ResponseEntity.ok(new ApiResponse<>("fetch_post_detail_success", response));
    }

    /**
     * 게시글 생성
     * POST /posts
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(@Validated @RequestBody PostRequest request) {
        // 예시로 PostService.createPost()가 생성된 게시글 ID를 반환한다고 가정
        Long postId = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_post_success", postId));
    }

    /**
     * 게시글 수정
     * PATCH /posts/{postId}
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable Long postId,
            @Validated @RequestBody PostRequest request) {
        // PostService.updatePost() 내부에서 접근한 사용자의 권한 검증을 수행한다고 가정
        postService.updatePost(postId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("update_post_success", null));
    }

    /**
     * 게시글 삭제
     * DELETE /posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("delete_post_success", null));
    }
}
