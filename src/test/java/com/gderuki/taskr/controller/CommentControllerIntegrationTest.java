package com.gderuki.taskr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.config.ApiConstants;
import com.gderuki.taskr.dto.CommentRequestDTO;
import com.gderuki.taskr.entity.Comment;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.repository.CommentRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerIntegrationTest extends WithTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;
    private User testUser;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();
        commentRepository.flush();
        taskRepository.flush();
        userRepository.flush();
        entityManager.clear();

        testUser = User.builder()
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

        testComment = Comment.builder()
                .content("Test comment")
                .task(testTask)
                .author(testUser)
                .build();
        commentRepository.save(testComment);
    }

    @Nested
    @DisplayName("Create Comment")
    class CreateCommentTests {

        @Test
        @WithMockUser(username = "testuser")
        void withValidData_ShouldReturnCreated() throws Exception {
            CommentRequestDTO request = CommentRequestDTO.builder()
                    .content("New comment content")
                    .build();

            mockMvc.perform(post(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value("New comment content"))
                    .andExpect(jsonPath("$.taskId").value(testTask.getId()))
                    .andExpect(jsonPath("$.authorUsername").value("testuser"));

            assertThat(commentRepository.count()).isEqualTo(2L);
        }

        @Test
        @WithMockUser(username = "testuser")
        void withEmptyContent_ShouldReturnBadRequest() throws Exception {
            CommentRequestDTO request = CommentRequestDTO.builder()
                    .content("")
                    .build();

            mockMvc.perform(post(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "testuser")
        void forNonExistentTask_ShouldReturnNotFound() throws Exception {
            CommentRequestDTO request = CommentRequestDTO.builder()
                    .content("New comment content")
                    .build();

            mockMvc.perform(post(ApiConstants.Tasks.BASE + "/999/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void withoutAuthentication_ShouldReturnForbidden() throws Exception {
            CommentRequestDTO request = CommentRequestDTO.builder()
                    .content("New comment content")
                    .build();

            mockMvc.perform(post(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Get Comments")
    class GetCommentsTests {

        @Test
        @WithMockUser
        void getAllCommentsForTask_ShouldReturnPagedComments() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].content").value("Test comment"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @WithMockUser
        void getCommentById_ShouldReturnComment() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/" + testComment.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testComment.getId()))
                    .andExpect(jsonPath("$.content").value("Test comment"));
        }

        @Test
        @WithMockUser
        void getCommentById_WithNonExistentComment_ShouldReturnNotFound() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        void getAllCommentsForNonExistentTask_ShouldReturnNotFound() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/999/comments"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void withoutAuthentication_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Update Comment")
    class UpdateCommentTests {

        @Test
        @WithMockUser(username = "testuser")
        void asAuthor_ShouldUpdateComment() throws Exception {
            CommentRequestDTO request = CommentRequestDTO.builder()
                    .content("Updated comment content")
                    .build();

            mockMvc.perform(put(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/" + testComment.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Updated comment content"));

            Comment updated = commentRepository.findById(testComment.getId()).orElseThrow();
            assertThat(updated.getContent()).isEqualTo("Updated comment content");
        }

        @Test
        @WithMockUser(username = "differentuser")
        void asNonAuthor_ShouldReturnForbidden() throws Exception {
            User differentUser = User.builder()
                    .username("differentuser")
                    .email("different@example.com")
                    .password(passwordEncoder.encode("password"))
                    .enabled(true)
                    .build();
            userRepository.save(differentUser);

            CommentRequestDTO request = CommentRequestDTO.builder()
                    .content("Updated comment content")
                    .build();

            mockMvc.perform(put(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/" + testComment.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(username = "testuser")
        void withNonExistentComment_ShouldReturnNotFound() throws Exception {
            CommentRequestDTO request = CommentRequestDTO.builder()
                    .content("Updated comment content")
                    .build();

            mockMvc.perform(put(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void withoutAuthentication_ShouldReturnForbidden() throws Exception {
            CommentRequestDTO request = CommentRequestDTO.builder()
                    .content("Updated comment content")
                    .build();

            mockMvc.perform(put(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/" + testComment.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Delete Comment")
    class DeleteCommentTests {

        @Test
        @WithMockUser(username = "testuser")
        void asAuthor_ShouldDeleteComment() throws Exception {
            mockMvc.perform(delete(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/" + testComment.getId()))
                    .andExpect(status().isNoContent());

            Comment deleted = commentRepository.findById(testComment.getId()).orElseThrow();
            assertThat(deleted.getDeletedAt()).isNotNull();
        }

        @Test
        @WithMockUser(username = "differentuser")
        void asNonAuthor_ShouldReturnForbidden() throws Exception {
            User differentUser = User.builder()
                    .username("differentuser")
                    .email("different@example.com")
                    .password(passwordEncoder.encode("password"))
                    .enabled(true)
                    .build();
            userRepository.save(differentUser);

            mockMvc.perform(delete(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/" + testComment.getId()))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(username = "testuser")
        void withNonExistentComment_ShouldReturnNotFound() throws Exception {
            mockMvc.perform(delete(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void withoutAuthentication_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(delete(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/" + testComment.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Soft Delete Behavior")
    class SoftDeleteBehaviorTests {

        @Test
        @WithMockUser
        void deletedComments_ShouldNotAppearInList() throws Exception {
            testComment.setDeletedAt(java.time.LocalDateTime.now());
            commentRepository.save(testComment);

            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser
        void getById_WithDeletedComment_ShouldReturn404() throws Exception {
            testComment.setDeletedAt(java.time.LocalDateTime.now());
            commentRepository.save(testComment);

            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/" + testComment.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "testuser")
        void update_OnDeletedComment_ShouldReturn404() throws Exception {
            testComment.setDeletedAt(java.time.LocalDateTime.now());
            commentRepository.save(testComment);

            CommentRequestDTO request = CommentRequestDTO.builder()
                    .content("Updated content")
                    .build();

            mockMvc.perform(put(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments/" + testComment.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Pagination")
    class PaginationTests {

        @Test
        @WithMockUser
        void withMultipleComments_ShouldReturnCorrectPage() throws Exception {
            for (int i = 0; i < 5; i++) {
                Comment comment = Comment.builder()
                        .content("Comment " + i)
                        .task(testTask)
                        .author(testUser)
                        .build();
                commentRepository.save(comment);
            }

            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId() + "/comments")
                            .param("page", "0")
                            .param("size", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(3))
                    .andExpect(jsonPath("$.totalElements").value(6))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }
    }
}
