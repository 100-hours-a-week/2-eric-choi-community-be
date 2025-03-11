package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.service.CommentService;
import com.amumal.community.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     * POST /posts/{postId}/comments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createComment(
            @PathVariable Long postId,
            @Validated @RequestBody CommentRequest request) {
        Long commentId = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_comment_success", commentId));
    }

    /**
     * 댓글 수정
     * PATCH /posts/{postId}/comments/{commentId}
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Validated @RequestBody CommentRequest request) {
        commentService.updateComment(postId, commentId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("update_comment_success", null));
    }

    /**
     * 댓글 삭제
     * DELETE /posts/{postId}/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("delete_comment_success", null));
    }
}
