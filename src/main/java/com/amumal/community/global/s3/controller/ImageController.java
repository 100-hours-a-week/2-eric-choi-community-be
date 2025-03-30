package com.amumal.community.global.s3.controller;

import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.global.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            if (image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("invalid_image", null));
            }

            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("invalid_image_type", null));
            }

            // 이미지 크기 제한 (예: 5MB)
            if (image.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("image_too_large", null));
            }

            // S3에 이미지 업로드
            String imageUrl = s3Service.uploadImage(image);

            return ResponseEntity.ok(new ApiResponse<>("upload_success", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>("upload_failed", null));
        }
    }
}