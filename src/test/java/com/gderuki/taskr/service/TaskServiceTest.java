package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.dto.TaskResponseDTO;
import com.gderuki.taskr.dto.TaskSearchCriteria;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.exception.TaskNotFoundException;
import com.gderuki.taskr.mapper.TaskMapper;
import com.gderuki.taskr.repository.TaskRepository;
import com.gderuki.taskr.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRequestDTO taskRequestDTO;
    private TaskResponseDTO taskResponseDTO;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        taskRequestDTO = TaskRequestDTO.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        taskResponseDTO = TaskResponseDTO.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();
    }

    @Test
    void createTask_ShouldReturnTaskResponseDTO() {
        when(taskMapper.toEntity(any(TaskRequestDTO.class))).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.createTask(taskRequestDTO);
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(taskRequestDTO.getTitle());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getAllTasks_ShouldReturnPageOfTaskResponseDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(taskRepository.findAllActive(pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.getAllTasks(pageable);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAllActive(pageable);
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTaskResponseDTO() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.getTaskById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldThrowException() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(1L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void updateTask_WhenTaskExists_ShouldReturnUpdatedTaskResponseDTO() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskMapper).updateEntityFromDto(any(TaskRequestDTO.class), any(Task.class));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.updateTask(1L, taskRequestDTO);
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_WhenTaskDoesNotExist_ShouldThrowException() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(1L, taskRequestDTO))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void deleteTask_WhenTaskExists_ShouldSoftDeleteTask() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.deleteTask(1L);
        assertThat(task.getDeletedAt()).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTask_WhenTaskDoesNotExist_ShouldThrowException() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(1L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void createTask_WithAssigneeId_ShouldAssignUserToTask() {
        TaskRequestDTO requestWithAssignee = TaskRequestDTO.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .assigneeId(1L)
                .build();

        when(taskMapper.toEntity(any(TaskRequestDTO.class))).thenReturn(task);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.createTask(requestWithAssignee);

        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WithInvalidAssigneeId_ShouldThrowException() {
        TaskRequestDTO requestWithAssignee = TaskRequestDTO.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .assigneeId(999L)
                .build();

        when(taskMapper.toEntity(any(TaskRequestDTO.class))).thenReturn(task);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(requestWithAssignee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void assignTask_WhenTaskAndUserExist_ShouldAssignUserToTask() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.assignTask(1L, 1L);

        assertThat(result).isNotNull();
        verify(taskRepository).findByIdAndNotDeleted(1L);
        verify(userRepository).findById(1L);
        verify(taskRepository).save(task);
    }

    @Test
    void assignTask_WhenTaskDoesNotExist_ShouldThrowException() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignTask(1L, 1L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void assignTask_WhenUserDoesNotExist_ShouldThrowException() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignTask(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void unassignTask_WhenTaskExists_ShouldRemoveAssignee() {
        task.setAssignee(user);
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.unassignTask(1L);

        assertThat(result).isNotNull();
        assertThat(task.getAssignee()).isNull();
        verify(taskRepository).findByIdAndNotDeleted(1L);
        verify(taskRepository).save(task);
    }

    @Test
    void unassignTask_WhenTaskDoesNotExist_ShouldThrowException() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.unassignTask(1L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void searchTasks_WithKeyword_ShouldReturnMatchingTasks() {
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .keyword("test")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    void searchTasks_WithStatus_ShouldReturnTasksWithStatus() {
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .status(TaskStatus.TODO)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    void searchTasks_WithPriority_ShouldReturnTasksWithPriority() {
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .priority(TaskPriority.HIGH)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    void searchTasks_WithAssigneeId_ShouldReturnAssignedTasks() {
        task.setAssignee(user);
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .assigneeId(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    void searchTasks_WithUnassignedOnly_ShouldReturnUnassignedTasks() {
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .unassignedOnly(true)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    void searchTasks_WithDueDateRange_ShouldReturnTasksInRange() {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(7);

        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .dueDateFrom(from)
                .dueDateTo(to)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    void searchTasks_WithCreatedDateRange_ShouldReturnTasksInRange() {
        LocalDateTime createdAfter = LocalDateTime.now().minusDays(7);
        LocalDateTime createdBefore = LocalDateTime.now();

        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .createdAfter(createdAfter)
                .createdBefore(createdBefore)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    void searchTasks_WithOverdueOnly_ShouldReturnOverdueTasks() {
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .overdueOnly(true)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    void searchTasks_WithMultipleCriteria_ShouldReturnMatchingTasks() {
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .keyword("test")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assigneeId(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }

    @Test
    void searchTasks_WithEmptyCriteria_ShouldReturnAllActiveTasks() {
        TaskSearchCriteria criteria = TaskSearchCriteria.builder().build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.searchTasks(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), eq(pageable));
    }
}
