package com.amumal.community.domain.post.controller;

import com.amumal.community.domain.post.dto.request.LikeRequest;
import com.amumal.community.domain.post.repository.likes.LikesRepository;
import com.amumal.community.domain.post.service.likes.LikesService;
import com.amumal.community.domain.user.service.UserQueryService;
import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.domain.user.entity.User;
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
    private final UserQueryService userQueryService;
    private final LikesRepository likesRepository;


    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addLike(
            @PathVariable Long postId,
            @RequestParam("email") String email,
            @Validated @RequestBody LikeRequest request) {
        User currentUser = userQueryService.getUserByEmail(email);
        likesService.addLike(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("like_success", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeLike(
            @PathVariable Long postId,
            @RequestParam("email") String email) {
        User currentUser = userQueryService.getUserByEmail(email);
        likesService.removeLike(postId, currentUser.getId(), currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>("unlike_success", null));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkLikeStatus(
            @PathVariable Long postId,
            @RequestParam("email") String email) {
        User currentUser = userQueryService.getUserByEmail(email);
        boolean isLiked = likesRepository.existsByPostIdAndUserId(postId, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>("success", isLiked));
    }
}
