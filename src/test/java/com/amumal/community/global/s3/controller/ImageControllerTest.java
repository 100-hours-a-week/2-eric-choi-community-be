package com.amumal.community.global.s3.controller;

import com.amumal.community.global.dto.ApiResponse;
import com.amumal.community.global.s3.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ImageController imageController;

    private MockMultipartFile validImage;
    private MockMultipartFile emptyImage;
    private MockMultipartFile invalidTypeImage;
    private MockMultipartFile largeImage;

    @BeforeEach
    void setUp() {
        // 유효한 이미지 파일 생성
        validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // 빈 이미지 파일 생성
        emptyImage = new MockMultipartFile(
                "image",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // 유효하지 않은 타입의 파일 생성
        invalidTypeImage = new MockMultipartFile(
                "image",
                "test.txt",
                "text/plain",
                "test text content".getBytes()
        );

        // 크기가 큰 이미지 파일 모의 생성
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        largeImage = new MockMultipartFile(
                "image",
                "large.jpg",
                "image/jpeg",
                largeContent
        );
    }

    @Test
    @DisplayName("이미지 업로드 성공 시 성공 응답 반환")
    void uploadImage_WithValidImage_ShouldReturnSuccess() throws IOException {
        // Given
        String expectedUrl = "https://s3-bucket.amazonaws.com/image.jpg";
        when(s3Service.uploadImage(any(MultipartFile.class))).thenReturn(expectedUrl);

        // When
        ResponseEntity<ApiResponse<String>> response = imageController.uploadImage(validImage, userDetails);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("upload_success", response.getBody().getMessage());
        assertEquals(expectedUrl, response.getBody().getData());
        verify(s3Service, times(1)).uploadImage(validImage);
    }

    @Test
    @DisplayName("빈 이미지 업로드 시 오류 응답 반환")
    void uploadImage_WithEmptyImage_ShouldReturnBadRequest() throws IOException {
        // When
        ResponseEntity<ApiResponse<String>> response = imageController.uploadImage(emptyImage, userDetails);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid_image", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(s3Service, never()).uploadImage(any(MultipartFile.class));
    }

    @Test
    @DisplayName("유효하지 않은 이미지 타입 업로드 시 오류 응답 반환")
    void uploadImage_WithInvalidImageType_ShouldReturnBadRequest() throws IOException {
        // When
        ResponseEntity<ApiResponse<String>> response = imageController.uploadImage(invalidTypeImage, userDetails);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid_image_type", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(s3Service, never()).uploadImage(any(MultipartFile.class));
    }

    @Test
    @DisplayName("크기가 큰 이미지 업로드 시 오류 응답 반환")
    void uploadImage_WithLargeImage_ShouldReturnBadRequest() throws IOException {
        // When
        ResponseEntity<ApiResponse<String>> response = imageController.uploadImage(largeImage, userDetails);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("image_too_large", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(s3Service, never()).uploadImage(any(MultipartFile.class));
    }

    @Test
    @DisplayName("이미지 업로드 중 IOException 발생 시 서버 오류 응답 반환")
    void uploadImage_WhenIOExceptionThrown_ShouldReturnInternalServerError() throws IOException {
        // Given
        when(s3Service.uploadImage(any(MultipartFile.class))).thenThrow(new IOException("Upload failed"));

        // When
        ResponseEntity<ApiResponse<String>> response = imageController.uploadImage(validImage, userDetails);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("upload_failed", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(s3Service, times(1)).uploadImage(validImage);
    }

    @Test
    @DisplayName("이미지 타입이 null인 경우 오류 응답 반환")
    void uploadImage_WithNullContentType_ShouldReturnBadRequest() throws IOException {
        // Given
        MockMultipartFile nullContentTypeImage = new MockMultipartFile(
                "image",
                "test.jpg",
                null,
                "test image content".getBytes()
        );

        // When
        ResponseEntity<ApiResponse<String>> response = imageController.uploadImage(nullContentTypeImage, userDetails);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid_image_type", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(s3Service, never()).uploadImage(any(MultipartFile.class));
    }
}