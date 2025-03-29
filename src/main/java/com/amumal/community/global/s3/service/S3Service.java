package com.amumal.community.global.s3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadImage(MultipartFile image) throws IOException {
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename(); // 고유한 파일 이름 생성

        // S3에 파일 업로드 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(image.getContentType())
                .build();

        // S3에 파일 업로드
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(image.getInputStream(), image.getSize()));

        return getPublicUrl(fileName);
    }

    private String getPublicUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
    }

    public void deleteImage(String imageUrl) {
        try {
            // URL에서 키(파일 이름) 추출 개선
            String key = extractKeyFromUrl(imageUrl);
            if (key != null) {
                // S3에서 파일 삭제
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
            }
        } catch (Exception e) {
            throw new RuntimeException("S3 이미지 삭제 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String extractKeyFromUrl(String imageUrl) {
        // 예시 URL: https://bucket-name.s3.region.amazonaws.com/file-name.jpg
        String bucketPrefix = bucket + ".s3." + region + ".amazonaws.com/";
        int startIndex = imageUrl.indexOf(bucketPrefix);
        if (startIndex != -1) {
            return imageUrl.substring(startIndex + bucketPrefix.length());
        }

        // 다른 형식의 URL에 대한 처리
        String altPrefix = "amazonaws.com/" + bucket + "/";
        startIndex = imageUrl.indexOf(altPrefix);
        if (startIndex != -1) {
            return imageUrl.substring(startIndex + altPrefix.length());
        }

        // 단순히 마지막 '/' 이후의 문자열을 키로 사용 (기존 방식)
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    public boolean isValidS3Url(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }

        return imageUrl.contains(bucket) && imageUrl.contains("amazonaws.com");
    }
}