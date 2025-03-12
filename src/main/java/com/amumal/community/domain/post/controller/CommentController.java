package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.service.comment.CommentService;
import com.amumal.community.domain.user.service.UserQueryService;
import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.domain.user.entity.User;
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
    private final UserQueryService userQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createComment(
            @PathVariable Long postId,
            @RequestParam("email") String email,
            @Validated @RequestBody CommentRequest request) {
        User currentUser = userQueryService.getUserByEmail(email);
        Long commentId = commentService.createComment(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_comment_success", commentId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam("email") String email,
            @Validated @RequestBody CommentRequest request) {
        User currentUser = userQueryService.getUserByEmail(email);
        commentService.updateComment(postId, commentId, request, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("update_comment_success", null));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam("email") String email) {
        User currentUser = userQueryService.getUserByEmail(email);
        commentService.deleteComment(postId, commentId, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("delete_comment_success", null));
    }
}
