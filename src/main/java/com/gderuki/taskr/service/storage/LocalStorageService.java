package com.gderuki.taskr.service.storage;

import com.gderuki.taskr.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path rootLocation;

    public LocalStorageService(@Value("${app.storage.local.root-path:uploads}") String rootPath) {
        this.rootLocation = Paths.get(rootPath);
    }

    @PostConstruct
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Local storage initialized at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(MultipartFile file, Long taskId) {
        String originalFilename = validateAndCleanFilename(file);

        try {
            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
            Path taskDirectory = rootLocation.resolve(String.valueOf(taskId));
            Files.createDirectories(taskDirectory);

            Path destinationFile = taskDirectory.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String storagePath = taskId + "/" + uniqueFilename;
            log.info("File stored successfully at: {}", storagePath);

            return storagePath;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFilename, e);
        }
    }

    @Override
    public Resource load(String storagePath) {
        try {
            Path file = rootLocation.resolve(storagePath);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Could not read file: " + storagePath);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Could not read file: " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path file = rootLocation.resolve(storagePath);
            Files.deleteIfExists(file);
            log.info("File deleted successfully: {}", storagePath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + storagePath, e);
        }
    }

    @Override
    public boolean exists(String storagePath) {
        Path file = rootLocation.resolve(storagePath);
        return Files.exists(file);
    }

    @Override
    public String getProviderName() {
        return "LOCAL";
    }
}
