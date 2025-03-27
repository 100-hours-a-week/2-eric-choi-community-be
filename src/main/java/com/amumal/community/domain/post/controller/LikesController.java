package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.repository.likes.LikesRepository;
import com.amumal.community.domain.post.service.likes.LikesService;
import com.amumal.community.domain.user.entity.User;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.config.security.JwtUserDetails;
import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.global.enums.CustomResponseStatus;
import com.amumal.community.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        // 인증되지 않은 사용자 체크
        if (userDetails == null) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        User currentUser = userService.findById(userDetails.getId());
        likesService.addLike(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("like_success", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        // 인증되지 않은 사용자 체크
        if (userDetails == null) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        User currentUser = userService.findById(userDetails.getId());
        likesService.removeLike(postId, currentUser.getId(), currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("unlike_success", null));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        // 인증되지 않은 사용자 체크
        if (userDetails == null) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        boolean isLiked = likesRepository.existsByPostIdAndUserId(postId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse<>("success", isLiked));
    }
}