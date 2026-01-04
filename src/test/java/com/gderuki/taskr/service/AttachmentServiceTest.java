package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.AttachmentResponseDTO;
import com.gderuki.taskr.entity.*;
import com.gderuki.taskr.exception.AttachmentNotFoundException;
import com.gderuki.taskr.exception.TaskNotFoundException;
import com.gderuki.taskr.repository.AttachmentRepository;
import com.gderuki.taskr.repository.TaskRepository;
import com.gderuki.taskr.repository.UserRepository;
import com.gderuki.taskr.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private AttachmentService attachmentService;

    private Task task;
    private Attachment attachment;
    private MultipartFile file;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        attachment = Attachment.builder()
                .id(1L)
                .fileName("uuid_test.txt")
                .originalFileName("test.txt")
                .contentType("text/plain")
                .fileSize(1024L)
                .storagePath("1/uuid_test.txt")
                .storageProvider("LOCAL")
                .task(task)
                .uploadedBy(user)
                .uploadedAt(LocalDateTime.now())
                .build();

        file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Test content".getBytes()
        );
    }

    @Test
    void uploadAttachment_shouldUploadSuccessfully() {
        // Given
        Long taskId = 1L;
        String storagePath = "1/uuid_test.txt";

        when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.of(task));
        when(storageService.store(file, taskId)).thenReturn(storagePath);
        when(storageService.getProviderName()).thenReturn("LOCAL");
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

        // When
        AttachmentResponseDTO result = attachmentService.uploadAttachment(taskId, file);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOriginalFileName()).isEqualTo("test.txt");
        assertThat(result.getTaskId()).isEqualTo(taskId);

        verify(taskRepository).findByIdAndNotDeleted(taskId);
        verify(storageService).store(file, taskId);
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    void uploadAttachment_shouldSetUploadedByUser_whenUserIsAuthenticated() {
        // Given
        Long taskId = 1L;
        String storagePath = "1/uuid_test.txt";

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        SecurityContextHolder.setContext(securityContext);

        when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.of(task));
        when(storageService.store(file, taskId)).thenReturn(storagePath);
        when(storageService.getProviderName()).thenReturn("LOCAL");
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
            Attachment savedAttachment = invocation.getArgument(0);
            assertThat(savedAttachment.getUploadedBy()).isEqualTo(user);
            return attachment;
        });

        // When
        attachmentService.uploadAttachment(taskId, file);

        // Then
        verify(userRepository).findByUsername("testuser");
        verify(attachmentRepository).save(argThat(att -> att.getUploadedBy() != null && att.getUploadedBy().equals(user)));

        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadAttachment_shouldHandleNoAuthenticatedUser() {
        // Given
        Long taskId = 1L;
        String storagePath = "1/uuid_test.txt";

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.of(task));
        when(storageService.store(file, taskId)).thenReturn(storagePath);
        when(storageService.getProviderName()).thenReturn("LOCAL");
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

        // When
        AttachmentResponseDTO result = attachmentService.uploadAttachment(taskId, file);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, never()).findByUsername(any());
        verify(attachmentRepository).save(argThat(att -> att.getUploadedBy() == null));

        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadAttachment_shouldThrowException_whenTaskNotFound() {
        // Given
        Long taskId = 999L;
        when(taskRepository.findByIdAndNotDeleted(taskId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> attachmentService.uploadAttachment(taskId, file))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository).findByIdAndNotDeleted(taskId);
        verify(storageService, never()).store(any(), any());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void getAttachmentsByTaskId_shouldReturnAttachments() {
        // Given
        Long taskId = 1L;
        Attachment attachment2 = Attachment.builder()
                .id(2L)
                .fileName("uuid_test2.txt")
                .originalFileName("test2.txt")
                .contentType("text/plain")
                .fileSize(2048L)
                .storagePath("1/uuid_test2.txt")
                .storageProvider("LOCAL")
                .task(task)
                .uploadedAt(LocalDateTime.now())
                .build();

        when(taskRepository.existsByIdAndNotDeleted(taskId)).thenReturn(true);
        when(attachmentRepository.findByTaskIdAndNotDeleted(taskId))
                .thenReturn(Arrays.asList(attachment, attachment2));

        // When
        List<AttachmentResponseDTO> results = attachmentService.getAttachmentsByTaskId(taskId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(1).getId()).isEqualTo(2L);

        verify(taskRepository).existsByIdAndNotDeleted(taskId);
        verify(attachmentRepository).findByTaskIdAndNotDeleted(taskId);
    }

    @Test
    void getAttachmentsByTaskId_shouldThrowException_whenTaskNotFound() {
        // Given
        Long taskId = 999L;
        when(taskRepository.existsByIdAndNotDeleted(taskId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> attachmentService.getAttachmentsByTaskId(taskId))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository).existsByIdAndNotDeleted(taskId);
        verify(attachmentRepository, never()).findByTaskIdAndNotDeleted(any());
    }

    @Test
    void getAttachment_shouldReturnAttachment() {
        // Given
        Long taskId = 1L;
        Long attachmentId = 1L;

        when(attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId))
                .thenReturn(Optional.of(attachment));

        // When
        AttachmentResponseDTO result = attachmentService.getAttachment(taskId, attachmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(attachmentId);
        assertThat(result.getTaskId()).isEqualTo(taskId);

        verify(attachmentRepository).findByIdAndTaskIdAndNotDeleted(attachmentId, taskId);
    }

    @Test
    void getAttachment_shouldThrowException_whenAttachmentNotFound() {
        // Given
        Long taskId = 1L;
        Long attachmentId = 999L;

        when(attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> attachmentService.getAttachment(taskId, attachmentId))
                .isInstanceOf(AttachmentNotFoundException.class);

        verify(attachmentRepository).findByIdAndTaskIdAndNotDeleted(attachmentId, taskId);
    }

    @Test
    void downloadAttachment_shouldReturnResource() {
        // Given
        Long taskId = 1L;
        Long attachmentId = 1L;
        Resource mockResource = mock(Resource.class);

        when(attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId))
                .thenReturn(Optional.of(attachment));
        when(storageService.load(attachment.getStoragePath())).thenReturn(mockResource);

        // When
        Resource result = attachmentService.downloadAttachment(taskId, attachmentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockResource);

        verify(attachmentRepository).findByIdAndTaskIdAndNotDeleted(attachmentId, taskId);
        verify(storageService).load(attachment.getStoragePath());
    }

    @Test
    void downloadAttachment_shouldThrowException_whenAttachmentNotFound() {
        // Given
        Long taskId = 1L;
        Long attachmentId = 999L;

        when(attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> attachmentService.downloadAttachment(taskId, attachmentId))
                .isInstanceOf(AttachmentNotFoundException.class);

        verify(attachmentRepository).findByIdAndTaskIdAndNotDeleted(attachmentId, taskId);
        verify(storageService, never()).load(any());
    }

    @Test
    void deleteAttachment_shouldDeleteSuccessfully() {
        // Given
        Long taskId = 1L;
        Long attachmentId = 1L;

        when(attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId))
                .thenReturn(Optional.of(attachment));
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
        doNothing().when(storageService).delete(attachment.getStoragePath());

        // When
        attachmentService.deleteAttachment(taskId, attachmentId);

        // Then
        verify(attachmentRepository).findByIdAndTaskIdAndNotDeleted(attachmentId, taskId);
        verify(attachmentRepository).save(argThat(att -> att.getDeletedAt() != null));
        verify(storageService).delete(attachment.getStoragePath());
    }

    @Test
    void deleteAttachment_shouldThrowException_whenAttachmentNotFound() {
        // Given
        Long taskId = 1L;
        Long attachmentId = 999L;

        when(attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> attachmentService.deleteAttachment(taskId, attachmentId))
                .isInstanceOf(AttachmentNotFoundException.class);

        verify(attachmentRepository).findByIdAndTaskIdAndNotDeleted(attachmentId, taskId);
        verify(attachmentRepository, never()).save(any());
        verify(storageService, never()).delete(any());
    }

    @Test
    void deleteAttachment_shouldContinue_whenStorageDeletionFails() {
        // Given
        Long taskId = 1L;
        Long attachmentId = 1L;

        when(attachmentRepository.findByIdAndTaskIdAndNotDeleted(attachmentId, taskId))
                .thenReturn(Optional.of(attachment));
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);
        doThrow(new RuntimeException("Storage error")).when(storageService).delete(attachment.getStoragePath());

        // When & Then
        assertThatCode(() -> attachmentService.deleteAttachment(taskId, attachmentId))
                .doesNotThrowAnyException();

        verify(attachmentRepository).findByIdAndTaskIdAndNotDeleted(attachmentId, taskId);
        verify(attachmentRepository).save(argThat(att -> att.getDeletedAt() != null));
        verify(storageService).delete(attachment.getStoragePath());
    }

    @Test
    void countAttachmentsByTaskId_shouldReturnCount() {
        // Given
        Long taskId = 1L;
        long expectedCount = 5L;

        when(taskRepository.existsByIdAndNotDeleted(taskId)).thenReturn(true);
        when(attachmentRepository.countByTaskIdAndNotDeleted(taskId)).thenReturn(expectedCount);

        // When
        long result = attachmentService.countAttachmentsByTaskId(taskId);

        // Then
        assertThat(result).isEqualTo(expectedCount);

        verify(taskRepository).existsByIdAndNotDeleted(taskId);
        verify(attachmentRepository).countByTaskIdAndNotDeleted(taskId);
    }

    @Test
    void countAttachmentsByTaskId_shouldThrowException_whenTaskNotFound() {
        // Given
        Long taskId = 999L;
        when(taskRepository.existsByIdAndNotDeleted(taskId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> attachmentService.countAttachmentsByTaskId(taskId))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository).existsByIdAndNotDeleted(taskId);
        verify(attachmentRepository, never()).countByTaskIdAndNotDeleted(any());
    }
}
