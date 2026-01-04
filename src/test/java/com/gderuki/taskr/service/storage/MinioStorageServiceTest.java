package com.gderuki.taskr.service.storage;

import com.gderuki.taskr.exception.FileStorageException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioStorageService storageService;

    @BeforeEach
    void setUp() throws Exception {
        String bucketName = "test-bucket";
        storageService = new MinioStorageService("http://localhost:9000", "testkey", "testsecret", bucketName);

        java.lang.reflect.Field minioClientField = MinioStorageService.class.getDeclaredField("minioClient");
        minioClientField.setAccessible(true);
        minioClientField.set(storageService, minioClient);
    }

    @Test
    void init_shouldCreateBucket_whenBucketDoesNotExist() throws Exception {
        // Given
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doAnswer(invocation -> null).when(minioClient).makeBucket(any(MakeBucketArgs.class));

        // When
        storageService.init();

        // Then
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void init_shouldNotCreateBucket_whenBucketExists() throws Exception {
        // Given
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        storageService.init();

        // Then
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void store_shouldStoreFileSuccessfully() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test content".getBytes()
        );
        Long taskId = 1L;

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));

        // When
        String storagePath = storageService.store(file, taskId);

        // Then
        assertThat(storagePath).isNotNull();
        assertThat(storagePath).startsWith(taskId + "/");
        assertThat(storagePath).contains("test.txt");
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void store_shouldThrowException_whenFileIsEmpty() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        // When & Then
        assertThatThrownBy(() -> storageService.store(emptyFile, 1L))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Cannot store empty file");
    }

    @Test
    void store_shouldThrowException_whenFilenameContainsRelativePath() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "../../../malicious.txt",
                "text/plain",
                "Test content".getBytes()
        );

        // When & Then
        assertThatThrownBy(() -> storageService.store(file, 1L))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Cannot store file with relative path");
    }

    @Test
    void load_shouldLoadFileSuccessfully() throws Exception {
        // Given
        String storagePath = "1/test.txt";
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);

        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        // When
        Resource resource = storageService.load(storagePath);

        // Then
        assertThat(resource).isNotNull();
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void delete_shouldDeleteFileSuccessfully() throws Exception {
        // Given
        String storagePath = "1/test.txt";
        doAnswer(invocation -> null).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // When & Then
        assertThatCode(() -> storageService.delete(storagePath))
                .doesNotThrowAnyException();

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void exists_shouldReturnTrue_whenFileExists() throws Exception {
        // Given
        String storagePath = "1/test.txt";
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mock(StatObjectResponse.class));

        // When
        boolean exists = storageService.exists(storagePath);

        // Then
        assertThat(exists).isTrue();
        verify(minioClient).statObject(any(StatObjectArgs.class));
    }

    @Test
    void exists_shouldReturnFalse_whenFileDoesNotExist() throws Exception {
        // Given
        String storagePath = "1/nonexistent.txt";

        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.code()).thenReturn("NoSuchKey");

        ErrorResponseException mockException = mock(ErrorResponseException.class);
        when(mockException.errorResponse()).thenReturn(errorResponse);

        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(mockException);

        // When
        boolean exists = storageService.exists(storagePath);

        // Then
        assertThat(exists).isFalse();
        verify(minioClient).statObject(any(StatObjectArgs.class));
    }

    @Test
    void getProviderName_shouldReturnMinio() {
        // When
        String providerName = storageService.getProviderName();

        // Then
        assertThat(providerName).isEqualTo("MINIO");
    }
}
