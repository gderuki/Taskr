package com.gderuki.taskr.service.storage;

import com.gderuki.taskr.exception.FileStorageException;
import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "minio")
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageService(
            @Value("${app.storage.minio.endpoint}") String endpoint,
            @Value("${app.storage.minio.access-key}") String accessKey,
            @Value("${app.storage.minio.secret-key}") String secretKey,
            @Value("${app.storage.minio.bucket-name}") String bucketName) {

        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    @Override
    public void init() {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("MinIO bucket created: {}", bucketName);
            } else {
                log.info("MinIO bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            throw new FileStorageException("Could not initialize MinIO storage", e);
        }
    }

    @Override
    public String store(MultipartFile file, Long taskId) {
        String originalFilename = validateAndCleanFilename(file);

        try {
            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
            String objectName = taskId + "/" + uniqueFilename;

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            log.info("File stored successfully in MinIO: {}", objectName);
            return objectName;

        } catch (IOException | ErrorResponseException | InsufficientDataException |
                 InternalException | InvalidKeyException | InvalidResponseException |
                 NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new FileStorageException("Failed to store file in MinIO: " + originalFilename, e);
        }
    }

    @Override
    public Resource load(String storagePath) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            );

            return new InputStreamResource(stream);

        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException |
                 NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new FileStorageException("Could not read file from MinIO: " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            );
            log.info("File deleted successfully from MinIO: {}", storagePath);

        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException |
                 NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new FileStorageException("Failed to delete file from MinIO: " + storagePath, e);
        }
    }

    @Override
    public boolean exists(String storagePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw new FileStorageException("Error checking file existence in MinIO: " + storagePath, e);
        } catch (InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            throw new FileStorageException("Error checking file existence in MinIO: " + storagePath, e);
        }
    }

    @Override
    public String getProviderName() {
        return "MINIO";
    }
}
