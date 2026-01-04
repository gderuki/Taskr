package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.dto.TaskResponseDTO;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.exception.TaskNotFoundException;
import com.gderuki.taskr.mapper.TaskMapper;
import com.gderuki.taskr.repository.TaskRepository;
import com.gderuki.taskr.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    /**
     * Create a new task
     */
    @Transactional
    @Timed(value = "taskr.task.create", description = "Time taken to create a task")
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        log.info("Creating new task with title: {}", taskRequestDTO.getTitle());

        Task task = taskMapper.toEntity(taskRequestDTO);

        if (taskRequestDTO.getAssigneeId() != null) {
            User assignee = userRepository.findById(taskRequestDTO.getAssigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + taskRequestDTO.getAssigneeId()));
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);

        log.info("Task created successfully with id: {}", savedTask.getId());
        return taskMapper.toDto(savedTask);
    }

    /**
     * Get all active (non-deleted) tasks with pagination and sorting
     */
    @Transactional(readOnly = true)
    @Timed(value = "taskr.task.getAll", description = "Time taken to fetch all tasks")
    public Page<TaskResponseDTO> getAllTasks(Pageable pageable) {
        log.info("Fetching all tasks with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Task> tasks = taskRepository.findAllActive(pageable);
        return tasks.map(taskMapper::toDto);
    }

    /**
     * Get a task by ID
     */
    @Transactional(readOnly = true)
    @Timed(value = "taskr.task.getById", description = "Time taken to fetch a task by ID")
    public TaskResponseDTO getTaskById(Long id) {
        log.info("Fetching task with id: {}", id);

        Task task = taskRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return taskMapper.toDto(task);
    }

    /**
     * Update an existing task
     */
    @Transactional
    @Timed(value = "taskr.task.update", description = "Time taken to update a task")
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO taskRequestDTO) {
        log.info("Updating task with id: {}", id);

        Task task = taskRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskMapper.updateEntityFromDto(taskRequestDTO, task);

        if (taskRequestDTO.getAssigneeId() != null) {
            User assignee = userRepository.findById(taskRequestDTO.getAssigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + taskRequestDTO.getAssigneeId()));
            task.setAssignee(assignee);
        }

        Task updatedTask = taskRepository.save(task);

        log.info("Task updated successfully with id: {}", id);
        return taskMapper.toDto(updatedTask);
    }

    /**
     * Soft delete a task (sets deletedAt timestamp)
     */
    @Transactional
    @Timed(value = "taskr.task.delete", description = "Time taken to delete a task")
    public void deleteTask(Long id) {
        log.info("Soft deleting task with id: {}", id);

        Task task = taskRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);

        log.info("Task soft deleted successfully with id: {}", id);
    }

    /**
     * Assign a task to a user
     */
    @Transactional
    @Timed(value = "taskr.task.assign", description = "Time taken to assign a task")
    public TaskResponseDTO assignTask(Long taskId, Long userId) {
        log.info("Assigning task {} to user {}", taskId, userId);

        Task task = taskRepository.findByIdAndNotDeleted(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        task.setAssignee(user);
        Task updatedTask = taskRepository.save(task);

        log.info("Task {} assigned to user {} successfully", taskId, userId);
        return taskMapper.toDto(updatedTask);
    }

    /**
     * Unassign a task (remove assignee)
     */
    @Transactional
    @Timed(value = "taskr.task.unassign", description = "Time taken to unassign a task")
    public TaskResponseDTO unassignTask(Long taskId) {
        log.info("Unassigning task {}", taskId);

        Task task = taskRepository.findByIdAndNotDeleted(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setAssignee(null);
        Task updatedTask = taskRepository.save(task);

        log.info("Task {} unassigned successfully", taskId);
        return taskMapper.toDto(updatedTask);
    }
}
