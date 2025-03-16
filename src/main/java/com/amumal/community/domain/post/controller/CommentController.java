package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.CommentRequest;
import com.amumal.community.domain.post.service.comment.CommentService;
import com.amumal.community.domain.user.service.UserQueryService;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.global.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createComment(
            @PathVariable Long postId,
            @Validated @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {

        User currentUser = SessionUtil.getCurrentUser(httpRequest, userService);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));
        }

        Long commentId = commentService.createComment(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_comment_success", commentId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Validated @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {

        User currentUser = SessionUtil.getCurrentUser(httpRequest, userService);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));
        }

        commentService.updateComment(postId, commentId, request, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("update_comment_success", null));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            HttpServletRequest httpRequest) {

        User currentUser = SessionUtil.getCurrentUser(httpRequest, userService);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));
        }

        commentService.deleteComment(postId, commentId, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("delete_comment_success", null));
    }
}