package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.repository.likes.LikesRepository;
import com.amumal.community.domain.post.service.likes.LikesService;
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
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikesController {

    private final LikesService likesService;
    private final UserService userService;
    private final LikesRepository likesRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addLike(
            @PathVariable Long postId,
            @Validated @RequestBody LikeRequest request,
            HttpServletRequest httpRequest) {

        User currentUser = SessionUtil.getCurrentUser(httpRequest, userService);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));
        }

        likesService.addLike(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("like_success", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeLike(
            @PathVariable Long postId,
            HttpServletRequest httpRequest) {

        User currentUser = SessionUtil.getCurrentUser(httpRequest, userService);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));
        }

        likesService.removeLike(postId, currentUser.getId(), currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("unlike_success", null));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkLikeStatus(
            @PathVariable Long postId,
            HttpServletRequest httpRequest) {

        User currentUser = SessionUtil.getCurrentUser(httpRequest, userService);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("unauthorized", null));
        }

        boolean isLiked = likesRepository.existsByPostIdAndUserId(postId, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>("success", isLiked));
    }
}