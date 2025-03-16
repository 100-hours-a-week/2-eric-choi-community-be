package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.service.post.PostCommandService;
import com.amumal.community.domain.post.service.post.PostQueryService;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserQueryService;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.global.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserService userService;  // UserQueryService 대신 UserService 사용

    @GetMapping
    public ResponseEntity<ApiResponse<PostResponse>> getPosts(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        PostResponse response = postQueryService.getPostSimpleInfo(cursor, pageSize);
        return ResponseEntity.ok(new ApiResponse<>("fetch_posts_success", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
            @PathVariable Long postId,
            HttpServletRequest request,
            @RequestParam(value = "incrementView", defaultValue = "true") Boolean incrementView) {

        // 세션 확인 없이 게시글 조회 가능 (선택적)
        Long userId = SessionUtil.getLoggedInUserId(request);
        User currentUser = userId != null ? userService.findById(userId) : null;

        PostDetailResponse response = postQueryService.getPostDetailInfoById(postId, incrementView);
        return ResponseEntity.ok(new ApiResponse<>("fetch_post_detail_success", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(
            @Validated @RequestBody PostRequest request,
            HttpServletRequest httpRequest) {

        User currentUser = SessionUtil.getCurrentUser(httpRequest, userService);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));
        }

        Long postId = postCommandService.createPost(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_post_success", postId));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable Long postId,
            @Validated @RequestBody PostRequest request,
            HttpServletRequest httpRequest) {

        User currentUser = SessionUtil.getCurrentUser(httpRequest, userService);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));
        }

        postCommandService.updatePost(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("update_post_success", null));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            HttpServletRequest httpRequest) {

        User currentUser = SessionUtil.getCurrentUser(httpRequest, userService);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));
        }

        postCommandService.deletePost(postId, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("delete_post_success", null));
    }
}
