package com.amumal.community.global.s3.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    private final String TEST_BUCKET = "test-bucket";
    private final String TEST_REGION = "ap-northeast-2";
    @Mock
    private S3Client s3Client;
    @InjectMocks
    private S3Service s3Service;
    private MockMultipartFile testImage;

    @BeforeEach
    void setUp() {
        // ReflectionTestUtils를 사용하여 private 필드에 값 주입
        ReflectionTestUtils.setField(s3Service, "bucket", TEST_BUCKET);
        ReflectionTestUtils.setField(s3Service, "region", TEST_REGION);

        // 테스트용 이미지 파일 생성
        testImage = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("이미지 업로드 성공 테스트")
    void uploadImage_ShouldReturnPublicUrl() throws IOException {
        // When
        String url = s3Service.uploadImage(testImage);

        // Then
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        assertNotNull(url);
        assertTrue(url.contains(TEST_BUCKET));
        assertTrue(url.contains(TEST_REGION));
        assertTrue(url.contains("amazonaws.com"));
    }

    @Test
    @DisplayName("이미지 업로드 시 IOException 전파 테스트")
    void uploadImage_ShouldPropagateIOException() throws IOException {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getInputStream()).thenThrow(new IOException("Test IO Exception"));

        // Then
        assertThrows(IOException.class, () -> s3Service.uploadImage(mockFile));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("이미지 삭제 성공 테스트 - 표준 URL 형식")
    void deleteImage_WithStandardUrl_ShouldCallS3ClientDelete() {
        // Given
        String imageUrl = "https://" + TEST_BUCKET + ".s3." + TEST_REGION + ".amazonaws.com/test-image.jpg";

        // When
        s3Service.deleteImage(imageUrl);

        // Then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("이미지 삭제 성공 테스트 - 대체 URL 형식")
    void deleteImage_WithAlternativeUrl_ShouldCallS3ClientDelete() {
        // Given
        String imageUrl = "https://amazonaws.com/" + TEST_BUCKET + "/test-image.jpg";

        // When
        s3Service.deleteImage(imageUrl);

        // Then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("이미지 삭제 성공 테스트 - 단순 URL 형식")
    void deleteImage_WithSimpleUrl_ShouldCallS3ClientDelete() {
        // Given
        String imageUrl = "https://some-endpoint/test-image.jpg";

        // When
        s3Service.deleteImage(imageUrl);

        // Then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("이미지 삭제 시 예외 처리 테스트")
    void deleteImage_WhenExceptionOccurs_ShouldThrowRuntimeException() {
        // Given
        String imageUrl = "https://" + TEST_BUCKET + ".s3." + TEST_REGION + ".amazonaws.com/test-image.jpg";
        doThrow(new RuntimeException("S3 delete failed")).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> s3Service.deleteImage(imageUrl));
        assertTrue(exception.getMessage().contains("S3 이미지 삭제 중 오류 발생"));
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("유효한 S3 URL 검증 테스트")
    void isValidS3Url_WithValidUrl_ShouldReturnTrue() {
        // Given
        String validUrl1 = "https://" + TEST_BUCKET + ".s3." + TEST_REGION + ".amazonaws.com/test-image.jpg";
        String validUrl2 = "https://amazonaws.com/" + TEST_BUCKET + "/test-image.jpg";

        // When & Then
        assertTrue(s3Service.isValidS3Url(validUrl1));
        assertTrue(s3Service.isValidS3Url(validUrl2));
    }

    @Test
    @DisplayName("유효하지 않은 S3 URL 검증 테스트")
    void isValidS3Url_WithInvalidUrl_ShouldReturnFalse() {
        // Given
        String invalidUrl1 = null;
        String invalidUrl2 = "";
        String invalidUrl3 = "https://example.com/image.jpg";

        // When & Then
        assertFalse(s3Service.isValidS3Url(invalidUrl1));
        assertFalse(s3Service.isValidS3Url(invalidUrl2));
        assertFalse(s3Service.isValidS3Url(invalidUrl3));
    }

    @Test
    @DisplayName("extractKeyFromUrl 메서드 테스트 - 표준 URL 형식")
    void extractKeyFromUrl_WithStandardUrl_ShouldExtractCorrectKey() {
        // Given
        String imageUrl = "https://" + TEST_BUCKET + ".s3." + TEST_REGION + ".amazonaws.com/test-image.jpg";

        // When
        String result = ReflectionTestUtils.invokeMethod(s3Service, "extractKeyFromUrl", imageUrl);

        // Then
        assertEquals("test-image.jpg", result);
    }

    @Test
    @DisplayName("extractKeyFromUrl 메서드 테스트 - 대체 URL 형식")
    void extractKeyFromUrl_WithAlternativeUrl_ShouldExtractCorrectKey() {
        // Given
        String imageUrl = "https://amazonaws.com/" + TEST_BUCKET + "/test-image.jpg";

        // When
        String result = ReflectionTestUtils.invokeMethod(s3Service, "extractKeyFromUrl", imageUrl);

        // Then
        assertEquals("test-image.jpg", result);
    }

    @Test
    @DisplayName("extractKeyFromUrl 메서드 테스트 - 단순 URL 형식")
    void extractKeyFromUrl_WithSimpleUrl_ShouldExtractCorrectKey() {
        // Given
        String imageUrl = "https://some-endpoint/test-image.jpg";

        // When
        String result = ReflectionTestUtils.invokeMethod(s3Service, "extractKeyFromUrl", imageUrl);

        // Then
        assertEquals("test-image.jpg", result);
    }
}