package com.gderuki.taskr.service.storage;

import com.gderuki.taskr.exception.FileStorageException;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage abstraction interface for file operations.
 * Allows pluggable storage implementations (Local, MinIO, S3, etc.)
 */
public interface StorageService {

    /**
     * Validates the uploaded file and returns a clean filename
     *
     * @param file the file to validate
     * @return cleaned filename
     * @throws FileStorageException if validation fails
     */
    default String validateAndCleanFilename(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new FileStorageException("File must have a valid filename");
        }

        String cleanedFilename = StringUtils.cleanPath(filename);

        if (cleanedFilename.contains("..")) {
            throw new FileStorageException("Cannot store file with relative path outside current directory: " + cleanedFilename);
        }

        return cleanedFilename;
    }

    /**
     * Store a file and return the storage path
     *
     * @param file the file to store
     * @param taskId the task ID associated with this file
     * @return the storage path where the file was stored
     */
    String store(MultipartFile file, Long taskId);

    /**
     * Load a file as a Resource
     *
     * @param storagePath the storage path of the file
     * @return the file as a Resource
     */
    Resource load(String storagePath);

    /**
     * Delete a file from storage
     *
     * @param storagePath the storage path of the file to delete
     */
    void delete(String storagePath);

    /**
     * Check if a file exists
     *
     * @param storagePath the storage path to check
     * @return true if the file exists, false otherwise
     */
    boolean exists(String storagePath);

    /**
     * Initialize the storage (create directories, buckets, etc.)
     */
    void init();

    /**
     * Get the storage provider identifier
     *
     * @return the storage provider name (e.g., "LOCAL", "MINIO", "S3")
     */
    String getProviderName();
}
