package com.gderuki.taskr.controller;

import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.config.ApiConstants;
import com.gderuki.taskr.entity.Attachment;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.repository.AttachmentRepository;
import com.gderuki.taskr.repository.TaskRepository;
import com.gderuki.taskr.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttachmentControllerIntegrationTest extends WithTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private Task testTask;

    @BeforeEach
    void setUp() {
        attachmentRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();
        attachmentRepository.flush();
        taskRepository.flush();
        userRepository.flush();
        entityManager.clear();

        User testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build();
        userRepository.save(testUser);

        testTask = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();
        taskRepository.save(testTask);
    }

    @Nested
    @DisplayName("Upload Attachment")
    class UploadAttachmentTests {

        @Test
        @WithMockUser
        void withValidFile_shouldReturnCreated() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    "text/plain",
                    "Test content".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments")
                            .file(file))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.originalFileName").value("test.txt"))
                    .andExpect(jsonPath("$.contentType").value("text/plain"))
                    .andExpect(jsonPath("$.fileSize").value(12))
                    .andExpect(jsonPath("$.taskId").value(testTask.getId()))
                    .andExpect(jsonPath("$.storageProvider").value("LOCAL"));
            assertThat(attachmentRepository.findByTaskIdAndNotDeleted(testTask.getId())).hasSize(1);
        }

        @Test
        @WithMockUser
        void withNonExistentTask_shouldReturnNotFound() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    "text/plain",
                    "Test content".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart(ApiConstants.Tasks.BASE + "/999/attachments")
                            .file(file))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get Attachments")
    class GetAttachmentsTests {

        @Test
        @WithMockUser
        void shouldReturnAllAttachments() throws Exception {
            // Given
            Attachment attachment1 = Attachment.builder()
                    .fileName("uuid_test1.txt")
                    .originalFileName("test1.txt")
                    .contentType("text/plain")
                    .fileSize(100L)
                    .storagePath("1/uuid_test1.txt")
                    .storageProvider("LOCAL")
                    .task(testTask)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            Attachment attachment2 = Attachment.builder()
                    .fileName("uuid_test2.txt")
                    .originalFileName("test2.txt")
                    .contentType("text/plain")
                    .fileSize(200L)
                    .storagePath("1/uuid_test2.txt")
                    .storageProvider("LOCAL")
                    .task(testTask)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            attachmentRepository.save(attachment1);
            attachmentRepository.save(attachment2);

            // When & Then
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].originalFileName").value("test1.txt"))
                    .andExpect(jsonPath("$[1].originalFileName").value("test2.txt"));
        }

        @Test
        @WithMockUser
        void withNonExistentTask_shouldReturnNotFound() throws Exception {
            // When & Then
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/999/attachments"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get Attachment")
    class GetAttachmentTests {

        @Test
        @WithMockUser
        void withValidId_shouldReturnAttachment() throws Exception {
            // Given
            Attachment attachment = Attachment.builder()
                    .fileName("uuid_test.txt")
                    .originalFileName("test.txt")
                    .contentType("text/plain")
                    .fileSize(100L)
                    .storagePath("1/uuid_test.txt")
                    .storageProvider("LOCAL")
                    .task(testTask)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            attachmentRepository.save(attachment);

            // When & Then
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments/" + attachment.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(attachment.getId()))
                    .andExpect(jsonPath("$.originalFileName").value("test.txt"));
        }

        @Test
        @WithMockUser
        void withNonExistentAttachment_shouldReturnNotFound() throws Exception {
            // When & Then
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Download Attachment")
    class DownloadAttachmentTests {

        @Test
        @WithMockUser
        void withValidId_shouldReturnFile() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    "text/plain",
                    "Test content".getBytes()
            );

            String response = mockMvc.perform(multipart(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments")
                            .file(file))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            long attachmentId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asLong();

            // When & Then
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments/" + attachmentId + "/download"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
                    .andExpect(content().contentType("text/plain"));
        }

        @Test
        @WithMockUser
        void withNonExistentAttachment_shouldReturnNotFound() throws Exception {
            // When & Then
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments/999/download"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Delete Attachment")
    class DeleteAttachmentTests {

        @Test
        @WithMockUser
        void withValidId_shouldReturnNoContent() throws Exception {
            // Given
            Attachment attachment = Attachment.builder()
                    .fileName("uuid_test.txt")
                    .originalFileName("test.txt")
                    .contentType("text/plain")
                    .fileSize(100L)
                    .storagePath("1/uuid_test.txt")
                    .storageProvider("LOCAL")
                    .task(testTask)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            attachmentRepository.save(attachment);

            // When & Then
            mockMvc.perform(delete(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments/" + attachment.getId()))
                    .andExpect(status().isNoContent());
            Attachment deletedAttachment = attachmentRepository.findById(attachment.getId()).orElseThrow();
            assertThat(deletedAttachment.getDeletedAt()).isNotNull();
        }

        @Test
        @WithMockUser
        void withNonExistentAttachment_shouldReturnNotFound() throws Exception {
            // When & Then
            mockMvc.perform(delete(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Count Attachments")
    class CountAttachmentsTests {

        @Test
        @WithMockUser
        void shouldReturnCount() throws Exception {
            // Given
            Attachment attachment1 = Attachment.builder()
                    .fileName("uuid_test1.txt")
                    .originalFileName("test1.txt")
                    .contentType("text/plain")
                    .fileSize(100L)
                    .storagePath("1/uuid_test1.txt")
                    .storageProvider("LOCAL")
                    .task(testTask)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            Attachment attachment2 = Attachment.builder()
                    .fileName("uuid_test2.txt")
                    .originalFileName("test2.txt")
                    .contentType("text/plain")
                    .fileSize(200L)
                    .storagePath("1/uuid_test2.txt")
                    .storageProvider("LOCAL")
                    .task(testTask)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            attachmentRepository.save(attachment1);
            attachmentRepository.save(attachment2);

            // When & Then
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/attachments/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("2"));
        }

        @Test
        @WithMockUser
        void withNonExistentTask_shouldReturnNotFound() throws Exception {
            // When & Then
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/999/attachments/count"))
                    .andExpect(status().isNotFound());
        }
    }
}
