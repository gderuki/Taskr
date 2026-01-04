package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.AttachmentResponseDTO;
import com.gderuki.taskr.entity.Attachment;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.exception.AttachmentNotFoundException;
import com.gderuki.taskr.exception.TaskNotFoundException;
import com.gderuki.taskr.repository.AttachmentRepository;
import com.gderuki.taskr.repository.TaskRepository;
import com.gderuki.taskr.repository.UserRepository;
import com.gderuki.taskr.service.storage.StorageService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Transactional
    @Timed(value = "taskr.attachment.upload", description = "Time taken to upload an attachment")
    public AttachmentResponseDTO uploadAttachment(Long taskId, MultipartFile file) {
        log.info("Uploading attachment for task: {}", taskId);

        Task task = taskRepository.findByIdAndNotDeleted(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        String storagePath = storageService.store(file, taskId);

        Attachment attachment = Attachment.builder()
                .fileName(extractFileName(storagePath))
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(storagePath)
                .storageProvider(storageService.getProviderName())
                .task(task)
                .uploadedBy(getCurrentUser())
                .build();

        Attachment savedAttachment = attachmentRepository.save(attachment);
        log.info("Attachment uploaded successfully with id: {}", savedAttachment.getId());

        return toDTO(savedAttachment);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.attachment.list", description = "Time taken to list attachments")
    public List<AttachmentResponseDTO> getAttachmentsByTaskId(Long taskId) {
        log.info("Fetching attachments for task: {}", taskId);

        if (!taskRepository.existsByIdAndNotDeleted(taskId)) {
            throw new TaskNotFoundException(taskId);
        }

        List<Attachment> attachments = attachmentRepository.findByTaskIdAndNotDeleted(taskId);
        return attachments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.attachment.get", description = "Time taken to get an attachment")
    public AttachmentResponseDTO getAttachment(Long taskId, Long attachmentId) {
        log.info("Fetching attachment with id: {} for task: {}", attachmentId, taskId);

        Attachment attachment = attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId)
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));

        return toDTO(attachment);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.attachment.download", description = "Time taken to download an attachment")
    public Resource downloadAttachment(Long taskId, Long attachmentId) {
        log.info("Downloading attachment with id: {} for task: {}", attachmentId, taskId);

        Attachment attachment = attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId)
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));

        return storageService.load(attachment.getStoragePath());
    }

    @Transactional
    @Timed(value = "taskr.attachment.delete", description = "Time taken to delete an attachment")
    public void deleteAttachment(Long taskId, Long attachmentId) {
        log.info("Deleting attachment with id: {} for task: {}", attachmentId, taskId);

        Attachment attachment = attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId)
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));

        attachment.setDeletedAt(LocalDateTime.now());
        attachmentRepository.save(attachment);

        try {
            storageService.delete(attachment.getStoragePath());
        } catch (Exception e) {
            log.warn("Failed to delete file from storage: {}", attachment.getStoragePath(), e);
        }

        log.info("Attachment deleted successfully with id: {}", attachmentId);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.attachment.count", description = "Time taken to count attachments")
    public long countAttachmentsByTaskId(Long taskId) {
        log.info("Counting attachments for task: {}", taskId);

        if (!taskRepository.existsByIdAndNotDeleted(taskId)) {
            throw new TaskNotFoundException(taskId);
        }

        return attachmentRepository.countByTaskIdAndNotDeleted(taskId);
    }

    private AttachmentResponseDTO toDTO(Attachment attachment) {
        return AttachmentResponseDTO.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .originalFileName(attachment.getOriginalFileName())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .storageProvider(attachment.getStorageProvider())
                .taskId(attachment.getTask().getId())
                .uploadedByUsername(attachment.getUploadedBy() != null ? attachment.getUploadedBy().getUsername() : null)
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    private String extractFileName(String storagePath) {
        int lastSlashIndex = storagePath.lastIndexOf('/');
        return lastSlashIndex >= 0 ? storagePath.substring(lastSlashIndex + 1) : storagePath;
    }
}
