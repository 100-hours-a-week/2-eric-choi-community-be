package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.service.LikesService;
import com.amumal.community.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikesController {

    private final LikesService likesService;

    /**
     * 게시글에 좋아요 추가
     * POST /posts/{postId}/likes
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addLike(
            @PathVariable Long postId,
            @Validated @RequestBody LikeRequest request) {
        // 좋아요 추가: 서비스에서 이미 해당 사용자가 좋아요를 눌렀는지 중복 체크할 수 있습니다.
        likesService.addLike(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("like_success", null));
    }

    /**
     * 게시글의 좋아요 취소
     * DELETE /posts/{postId}/likes?userId=...
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeLike(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        likesService.removeLike(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("unlike_success", null));
    }
}
