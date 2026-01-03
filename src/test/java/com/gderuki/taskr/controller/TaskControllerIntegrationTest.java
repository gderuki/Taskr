package com.gderuki.taskr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.config.ApiConstants;
import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.entity.User;
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
class TaskControllerIntegrationTest extends WithTestContainer {

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
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
                .build();
        taskRepository.save(testTask);
    }

    @Nested
    @DisplayName("Create Task")
    class CreateTaskTests {

        @Test
        @WithMockUser
        void withValidData_ShouldReturnCreated() throws Exception {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("New Task")
                    .description("New Description")
                    .status(TaskStatus.TODO)
                    .build();

            mockMvc.perform(post(ApiConstants.Tasks.BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("New Task"));

            assertThat(taskRepository.findAll()).hasSize(2);
        }

        @Test
        @WithMockUser
        void withEmptyTitle_ShouldReturn400() throws Exception {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("")
                    .status(TaskStatus.TODO)
                    .build();

            mockMvc.perform(post(ApiConstants.Tasks.BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.title").exists());
        }

        @Test
        @WithMockUser
        void withNullStatus_ShouldReturn400() throws Exception {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("Task without status")
                    .build();

            mockMvc.perform(post(ApiConstants.Tasks.BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.status").value("Status is required"));
        }

        @Test
        void whenUnauthenticated_ShouldReturn403() throws Exception {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("Task")
                    .status(TaskStatus.TODO)
                    .build();

            mockMvc.perform(post(ApiConstants.Tasks.BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Get Tasks")
    class GetTasksTests {

        @Test
        @WithMockUser
        void getAll_ShouldReturnPagedTasks() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Test Task"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @WithMockUser
        void getById_WithExistingTask_ShouldReturnTask() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Test Task"));
        }

        @Test
        @WithMockUser
        void getById_WithNonExistentId_ShouldReturn404() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getAll_WhenUnauthenticated_ShouldReturn403() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Update Task")
    class UpdateTaskTests {

        @Test
        @WithMockUser
        void withValidData_ShouldReturnUpdatedTask() throws Exception {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            mockMvc.perform(put(ApiConstants.Tasks.BASE + "/" + testTask.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"));

            Task updated = taskRepository.findById(testTask.getId()).orElseThrow();
            assertThat(updated.getTitle()).isEqualTo("Updated Title");
        }

        @Test
        @WithMockUser
        void withNonExistentId_ShouldReturn404() throws Exception {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("Updated")
                    .status(TaskStatus.TODO)
                    .build();

            mockMvc.perform(put(ApiConstants.Tasks.BASE + "/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Delete Task")
    class DeleteTaskTests {

        @Test
        @WithMockUser
        void withExistingTask_ShouldSoftDelete() throws Exception {
            Long taskId = testTask.getId();

            mockMvc.perform(delete(ApiConstants.Tasks.BASE + "/" + taskId))
                    .andExpect(status().isNoContent());

            Task deletedTask = taskRepository.findById(taskId).orElseThrow();
            assertThat(deletedTask.getDeletedAt()).isNotNull();
        }

        @Test
        @WithMockUser
        void withNonExistentId_ShouldReturn404() throws Exception {
            mockMvc.perform(delete(ApiConstants.Tasks.BASE + "/999999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Pagination and Sorting")
    class PaginationAndSortingTests {

        @BeforeEach
        void setUpMultipleTasks() {
            for (int i = 1; i <= 5; i++) {
                Task task = Task.builder()
                        .title("Task " + i)
                        .status(TaskStatus.TODO)
                        .build();
                taskRepository.save(task);
            }
        }

        @Test
        @WithMockUser
        void withPagination_ShouldReturnCorrectPage() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE)
                            .param("page", "0")
                            .param("size", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(3))
                    .andExpect(jsonPath("$.totalElements").value(6))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Test
        @WithMockUser
        void withSorting_ShouldReturnSortedTasks() throws Exception {
            Task taskA = Task.builder().title("A Task").status(TaskStatus.TODO).build();
            Task taskZ = Task.builder().title("Z Task").status(TaskStatus.TODO).build();
            taskRepository.save(taskZ);
            taskRepository.save(taskA);

            mockMvc.perform(get(ApiConstants.Tasks.BASE)
                            .param("sortBy", "title")
                            .param("direction", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("A Task"));
        }
    }

    @Nested
    @DisplayName("Soft Delete Behavior")
    class SoftDeleteBehaviorTests {

        @Test
        @WithMockUser
        void deletedTasks_ShouldNotAppearInList() throws Exception {
            testTask.setDeletedAt(java.time.LocalDateTime.now());
            taskRepository.save(testTask);

            mockMvc.perform(get(ApiConstants.Tasks.BASE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser
        void getById_WithDeletedTask_ShouldReturn404() throws Exception {
            testTask.setDeletedAt(java.time.LocalDateTime.now());
            taskRepository.save(testTask);

            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/" + testTask.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        void update_OnDeletedTask_ShouldReturn404() throws Exception {
            testTask.setDeletedAt(java.time.LocalDateTime.now());
            taskRepository.save(testTask);

            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("Updated")
                    .status(TaskStatus.TODO)
                    .build();

            mockMvc.perform(put(ApiConstants.Tasks.BASE + "/" + testTask.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}
