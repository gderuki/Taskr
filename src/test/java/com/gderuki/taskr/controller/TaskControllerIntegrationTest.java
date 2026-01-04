package com.gderuki.taskr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.config.ApiConstants;
import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.repository.TaskRepository;
import com.gderuki.taskr.repository.UserRepository;
import com.gderuki.taskr.security.WithMockCustomUser;
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

import java.time.LocalDateTime;

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
                .priority(TaskPriority.MEDIUM)
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
                    .priority(TaskPriority.HIGH)
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
                    .priority(TaskPriority.MEDIUM)
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
                    .priority(TaskPriority.MEDIUM)
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
                    .priority(TaskPriority.URGENT)
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
                    .priority(TaskPriority.MEDIUM)
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
                        .priority(TaskPriority.LOW)
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
            Task taskA = Task.builder().title("A Task").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build();
            Task taskZ = Task.builder().title("Z Task").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build();
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
                    .priority(TaskPriority.MEDIUM)
                    .build();

            mockMvc.perform(put(ApiConstants.Tasks.BASE + "/" + testTask.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Search and Filter Tasks")
    class SearchAndFilterTasksTests {

        private User testUser;

        @BeforeEach
        void setUpSearchTestData() {
            testUser = userRepository.findAll().getFirst();

            Task task1 = Task.builder()
                    .title("Important Documentation Task")
                    .description("Write API documentation")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .assignee(testUser)
                    .dueDate(LocalDateTime.now().plusDays(5))
                    .build();
            taskRepository.save(task1);

            Task task2 = Task.builder()
                    .title("Bug Fix Required")
                    .description("Fix authentication bug")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.URGENT)
                    .assignee(testUser)
                    .dueDate(LocalDateTime.now().plusDays(1))
                    .build();
            taskRepository.save(task2);

            Task task3 = Task.builder()
                    .title("Feature Development")
                    .description("Implement new feature")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(LocalDateTime.now().plusDays(10))
                    .build();
            taskRepository.save(task3);

            Task task4 = Task.builder()
                    .title("Completed Documentation")
                    .description("Documentation completed")
                    .status(TaskStatus.DONE)
                    .priority(TaskPriority.LOW)
                    .dueDate(LocalDateTime.now().minusDays(2))
                    .build();
            taskRepository.save(task4);

            Task task5 = Task.builder()
                    .title("Overdue Task")
                    .description("This is overdue")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .dueDate(LocalDateTime.now().minusDays(5))
                    .build();
            taskRepository.save(task5);
        }

        @Test
        @WithMockUser
        void searchByKeyword_InTitle_ShouldReturnMatchingTasks() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("keyword", "documentation"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[?(@.title =~ /.*Documentation.*/i)]").exists());
        }

        @Test
        @WithMockUser
        void searchByKeyword_InDescription_ShouldReturnMatchingTasks() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("keyword", "authentication"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].description").value("Fix authentication bug"));
        }

        @Test
        @WithMockUser
        void searchByStatus_ShouldReturnTasksWithStatus() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("status", "TODO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.status == 'TODO')]").isArray());
        }

        @Test
        @WithMockUser
        void searchByPriority_ShouldReturnTasksWithPriority() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("priority", "HIGH"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.priority == 'HIGH')]").isArray());
        }

        @Test
        @WithMockUser
        void searchByAssigneeId_ShouldReturnAssignedTasks() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("assigneeId", testUser.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        @WithMockUser
        void searchUnassignedOnly_ShouldReturnUnassignedTasks() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("unassignedOnly", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.assigneeId != null)]").isEmpty());
        }

        @Test
        @WithMockUser
        void searchByDueDateRange_ShouldReturnTasksInRange() throws Exception {
            LocalDateTime from = LocalDateTime.now();
            LocalDateTime to = LocalDateTime.now().plusDays(7);

            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("dueDateFrom", from.toString())
                            .param("dueDateTo", to.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser
        void searchByCreatedDateRange_ShouldReturnTasksInRange() throws Exception {
            LocalDateTime createdAfter = LocalDateTime.now().minusHours(1);
            LocalDateTime createdBefore = LocalDateTime.now().plusHours(1);

            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("createdAfter", createdAfter.toString())
                            .param("createdBefore", createdBefore.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @WithMockUser
        void searchOverdueOnly_ShouldReturnOverdueTasks() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("overdueOnly", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.status == 'DONE')]").isEmpty());
        }

        @Test
        @WithMockUser
        void searchWithMultipleCriteria_ShouldReturnMatchingTasks() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("keyword", "bug")
                            .param("status", "IN_PROGRESS")
                            .param("priority", "URGENT")
                            .param("assigneeId", testUser.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Bug Fix Required"));
        }

        @Test
        @WithMockUser
        void searchWithPaginationAndSorting_ShouldReturnPagedSortedResults() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("status", "TODO")
                            .param("page", "0")
                            .param("size", "2")
                            .param("sortBy", "priority")
                            .param("direction", "DESC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageable.pageSize").value(2));
        }

        @Test
        @WithMockUser
        void searchWithEmptyCriteria_ShouldReturnAllActiveTasks() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(6));
        }

        @Test
        @WithMockUser
        void searchExcludesDeletedTasks_EvenWithMatchingCriteria() throws Exception {
            Task deletedTask = Task.builder()
                    .title("Deleted Documentation Task")
                    .description("This should not appear")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .deletedAt(LocalDateTime.now())
                    .build();
            taskRepository.save(deletedTask);

            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("keyword", "documentation"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.title == 'Deleted Documentation Task')]").isEmpty());
        }

        @Test
        void searchWhenUnauthenticated_ShouldReturn403() throws Exception {
            mockMvc.perform(get(ApiConstants.Tasks.BASE + "/search")
                            .param("keyword", "test"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Audit Logging Tests")
    class AuditLoggingTests {

        @Test
        @WithMockCustomUser()
        void deleteTask_ShouldSetDeletedBy() throws Exception {
            Long taskId = testTask.getId();

            mockMvc.perform(delete(ApiConstants.Tasks.BASE + "/" + taskId))
                    .andExpect(status().isNoContent());

            Task deletedTask = taskRepository.findById(taskId).orElseThrow();
            assertThat(deletedTask.getDeletedAt()).isNotNull();
            assertThat(deletedTask.getDeletedBy()).isNotNull();
            assertThat(deletedTask.getDeletedBy()).isEqualTo(1L);
        }
    }
}
