package com.bankjatim.jims.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name:bank-jatim-jims}")
    private String bucketName;

    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO bucket '{}' successfully created.", bucketName);
            }
        } catch (Exception e) {
            log.warn("MinIO bucket check failed (MinIO service might not be running yet): {}", e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file, String folder) {
        try {
            initBucket();
            String filename = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filename)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }
            return filename;
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage());
            return "local-fallback/" + file.getOriginalFilename();
        }
    }

    public String getPresignedDownloadUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            return "/api/files/" + objectName;
        }
    }
}
