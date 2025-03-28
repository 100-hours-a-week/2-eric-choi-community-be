package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.PostRequest;
import com.amumal.community.domain.post.dto.response.PostDetailResponse;
import com.amumal.community.domain.post.dto.response.PostResponse;
import com.amumal.community.domain.post.service.post.PostCommandService;
import com.amumal.community.domain.post.service.post.PostQueryService;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.config.security.JwtUserDetails;
import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final UserService userService;

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
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(value = "incrementView", defaultValue = "true") Boolean incrementView) {

        // 인증된 사용자가 없어도 게시글 조회는 가능
        PostDetailResponse response = postQueryService.getPostDetailInfoById(postId, incrementView);
        return ResponseEntity.ok(new ApiResponse<>("fetch_post_detail_success", response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> createPost(
            @RequestPart("postInfo") @Validated PostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        User currentUser = userService.findById(userDetails.getId());
        System.out.println("request = " + request);
        Long postId = postCommandService.createPost(request, image, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_post_success", postId));
    }

    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable Long postId,
            @RequestPart("postInfo") @Validated PostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        User currentUser = userService.findById(userDetails.getId());
        postCommandService.updatePost(postId, request, image, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("update_post_success", null));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        if (userDetails == null) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        User currentUser = userService.findById(userDetails.getId());
        postCommandService.deletePost(postId, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("delete_post_success", null));
    }
}