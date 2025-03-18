package com.amumal.community.domain.user.controller;

import com.amumal.community.domain.user.dto.request.PasswordUpdateRequest;
import com.amumal.community.domain.user.dto.request.UserUpdateRequest;
import com.amumal.community.domain.user.dto.response.UserInfoResponse;
import com.amumal.community.domain.user.service.UserService;
import com.amumal.community.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(@PathVariable("id") Long id) {
        UserInfoResponse userInfo = userService.getUserInfo(id);
        ApiResponse<UserInfoResponse> response = new ApiResponse<>("fetch_user_success", userInfo);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @PathVariable("id") Long id,
            @RequestPart(value = "userInfo") @Validated UserUpdateRequest updateRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        if (!id.equals(updateRequest.getUserId())) {
            throw new IllegalArgumentException("User ID mismatch between path and request body");
        }

        userService.updateProfile(updateRequest, profileImage);
        ApiResponse<Void> response = new ApiResponse<>("profile_update_success", null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable("id") Long id,
            @Validated @RequestBody PasswordUpdateRequest passwordUpdateRequest) {

        if (!id.equals(passwordUpdateRequest.getUserId())) {
            throw new IllegalArgumentException("User ID mismatch between path and request body");
        }

        userService.updatePassword(passwordUpdateRequest);
        ApiResponse<Void> response = new ApiResponse<>("password_update_success", null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        ApiResponse<Void> response = new ApiResponse<>("user_delete_success", null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}