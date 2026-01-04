package com.gderuki.taskr.service.storage;

import com.gderuki.taskr.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalStorageService(tempDir.toString());
        storageService.init();
    }

    @Test
    void init_shouldCreateRootDirectory() {
        assertThat(Files.exists(tempDir)).isTrue();
        assertThat(Files.isDirectory(tempDir)).isTrue();
    }

    @Test
    void store_shouldStoreFileSuccessfully() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test content".getBytes()
        );
        Long taskId = 1L;

        // When
        String storagePath = storageService.store(file, taskId);

        // Then
        assertThat(storagePath).isNotNull();
        assertThat(storagePath).startsWith(taskId + "/");
        assertThat(storagePath).contains("test.txt");
        assertThat(storageService.exists(storagePath)).isTrue();
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
    void load_shouldLoadFileSuccessfully() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test content".getBytes()
        );
        Long taskId = 1L;
        String storagePath = storageService.store(file, taskId);

        // When
        Resource resource = storageService.load(storagePath);

        // Then
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void load_shouldThrowException_whenFileDoesNotExist() {
        // Given
        String nonExistentPath = "1/nonexistent.txt";

        // When & Then
        assertThatThrownBy(() -> storageService.load(nonExistentPath))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Could not read file");
    }

    @Test
    void delete_shouldDeleteFileSuccessfully() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test content".getBytes()
        );
        Long taskId = 1L;
        String storagePath = storageService.store(file, taskId);
        assertThat(storageService.exists(storagePath)).isTrue();

        // When
        storageService.delete(storagePath);

        // Then
        assertThat(storageService.exists(storagePath)).isFalse();
    }

    @Test
    void delete_shouldNotThrowException_whenFileDoesNotExist() {
        // Given
        String nonExistentPath = "1/nonexistent.txt";

        // When & Then
        assertThatCode(() -> storageService.delete(nonExistentPath))
                .doesNotThrowAnyException();
    }

    @Test
    void exists_shouldReturnTrue_whenFileExists() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test content".getBytes()
        );
        Long taskId = 1L;
        String storagePath = storageService.store(file, taskId);

        // When
        boolean exists = storageService.exists(storagePath);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void exists_shouldReturnFalse_whenFileDoesNotExist() {
        // Given
        String nonExistentPath = "1/nonexistent.txt";

        // When
        boolean exists = storageService.exists(nonExistentPath);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void getProviderName_shouldReturnLocal() {
        // When
        String providerName = storageService.getProviderName();

        // Then
        assertThat(providerName).isEqualTo("LOCAL");
    }

    @Test
    void store_shouldCreateTaskDirectoryIfNotExists() {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test content".getBytes()
        );
        Long taskId = 999L;

        // When
        storageService.store(file, taskId);

        // Then
        Path taskDirectory = tempDir.resolve(String.valueOf(taskId));
        assertThat(Files.exists(taskDirectory)).isTrue();
        assertThat(Files.isDirectory(taskDirectory)).isTrue();
    }

    @Test
    void store_shouldGenerateUniqueFilenames() {
        // Given
        MultipartFile file1 = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Content 1".getBytes()
        );
        MultipartFile file2 = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Content 2".getBytes()
        );
        Long taskId = 1L;

        // When
        String storagePath1 = storageService.store(file1, taskId);
        String storagePath2 = storageService.store(file2, taskId);

        // Then
        assertThat(storagePath1).isNotEqualTo(storagePath2);
        assertThat(storageService.exists(storagePath1)).isTrue();
        assertThat(storageService.exists(storagePath2)).isTrue();
    }
}
