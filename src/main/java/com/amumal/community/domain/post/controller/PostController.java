package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.service.post.PostCommandService;
import com.amumal.community.domain.post.service.post.PostQueryService;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserQueryService;
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

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final UserQueryService userQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PostResponse>> getPosts(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        PostResponse response = postQueryService.getPostSimpleInfo(cursor, pageSize);
        return ResponseEntity.ok(new ApiResponse<>("fetch_posts_success", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(@PathVariable Long postId) {
        PostDetailResponse response = postQueryService.getPostDetailInfoById(postId);
        return ResponseEntity.ok(new ApiResponse<>("fetch_post_detail_success", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(
            @Validated @RequestBody PostRequest request,
            @RequestParam("email") String email) {
        User currentUser = userQueryService.getUserByEmail(email);
        Long postId = postCommandService.createPost(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_post_success", postId));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable Long postId,
            @Validated @RequestBody PostRequest request,
            @RequestParam("email") String email) {
        User currentUser = userQueryService.getUserByEmail(email);
        postCommandService.updatePost(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("update_post_success", null));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @RequestParam("email") String email) {
        User currentUser = userQueryService.getUserByEmail(email);
        postCommandService.deletePost(postId, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("delete_post_success", null));
    }
}
