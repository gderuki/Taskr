package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.dto.TaskResponseDTO;
import com.gderuki.taskr.dto.TaskSearchCriteria;
import com.gderuki.taskr.entity.Tag;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.exception.TaskNotFoundException;
import com.gderuki.taskr.mapper.TaskMapper;
import com.gderuki.taskr.repository.TaskRepository;
import com.gderuki.taskr.repository.UserRepository;
import com.gderuki.taskr.security.CustomUserDetails;
import com.gderuki.taskr.specification.TaskSpecification;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TagService tagService;

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

        if (taskRequestDTO.getTagIds() != null && !taskRequestDTO.getTagIds().isEmpty()) {
            Set<Tag> tags = tagService.getTagsByIds(taskRequestDTO.getTagIds());
            task.setTags(tags);
        }

        Task savedTask = taskRepository.save(task);

        log.info("Task created successfully with id: {}", savedTask.getId());
        return taskMapper.toDto(savedTask);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.task.getAll", description = "Time taken to fetch all tasks")
    public Page<TaskResponseDTO> getAllTasks(Pageable pageable) {
        log.info("Fetching all tasks with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Task> tasks = taskRepository.findAllActive(pageable);
        return tasks.map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.task.getById", description = "Time taken to fetch a task by ID")
    public TaskResponseDTO getTaskById(Long id) {
        log.info("Fetching task with id: {}", id);

        Task task = taskRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return taskMapper.toDto(task);
    }

    @Transactional
    @Timed(value = "taskr.task.update", description = "Time taken to update a task")
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO taskRequestDTO) {
        log.info("Updating task with id: {}", id);

        Task task = taskRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskMapper.updateEntityFromDto(taskRequestDTO, task);

        getCurrentUserId().ifPresent(task::setModifiedBy);

        if (taskRequestDTO.getAssigneeId() != null) {
            User assignee = userRepository.findById(taskRequestDTO.getAssigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + taskRequestDTO.getAssigneeId()));
            task.setAssignee(assignee);
        }

        if (taskRequestDTO.getTagIds() != null) {
            if (taskRequestDTO.getTagIds().isEmpty()) {
                task.getTags().clear();
            } else {
                Set<Tag> tags = tagService.getTagsByIds(taskRequestDTO.getTagIds());
                task.setTags(tags);
            }
        }

        Task updatedTask = taskRepository.save(task);

        log.info("Task updated successfully with id: {}", id);
        log.debug("Task modifiedBy: {}", updatedTask.getModifiedBy());
        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    @Timed(value = "taskr.task.delete", description = "Time taken to delete a task")
    public void deleteTask(Long id) {
        log.info("Soft deleting task with id: {}", id);

        Task task = taskRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setDeletedAt(LocalDateTime.now());

        getCurrentUserId().ifPresent(task::setDeletedBy);

        taskRepository.save(task);

        log.info("Task soft deleted successfully with id: {}", id);
    }

    private Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return java.util.Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return java.util.Optional.of(userDetails.getId());
        } else if (principal instanceof User user) {
            return java.util.Optional.of(user.getId());
        }

        return java.util.Optional.empty();
    }

    @Transactional
    @Timed(value = "taskr.task.assign", description = "Time taken to assign a task")
    public TaskResponseDTO assignTask(Long taskId, Long userId) {
        log.info("Assigning task {} to user {}", taskId, userId);

        Task task = taskRepository.findByIdAndNotDeleted(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        task.setAssignee(user);
        getCurrentUserId().ifPresent(task::setModifiedBy);
        Task updatedTask = taskRepository.save(task);

        log.info("Task {} assigned to user {} successfully", taskId, userId);
        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    @Timed(value = "taskr.task.unassign", description = "Time taken to unassign a task")
    public TaskResponseDTO unassignTask(Long taskId) {
        log.info("Unassigning task {}", taskId);

        Task task = taskRepository.findByIdAndNotDeleted(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setAssignee(null);
        getCurrentUserId().ifPresent(task::setModifiedBy);
        Task updatedTask = taskRepository.save(task);

        log.info("Task {} unassigned successfully", taskId);
        return taskMapper.toDto(updatedTask);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.task.search", description = "Time taken to search tasks")
    public Page<TaskResponseDTO> searchTasks(TaskSearchCriteria criteria, Pageable pageable) {
        log.info("Searching tasks with criteria: {}", criteria);

        Specification<Task> specification = TaskSpecification.withCriteria(criteria);
        Page<Task> tasks = taskRepository.findAll(specification, pageable);

        log.info("Found {} tasks matching search criteria", tasks.getTotalElements());
        return tasks.map(taskMapper::toDto);
    }

    @Transactional
    @Timed(value = "taskr.task.addTag", description = "Time taken to add a tag to a task")
    public TaskResponseDTO addTagToTask(Long taskId, Long tagId) {
        log.info("Adding tag {} to task {}", tagId, taskId);

        Task task = taskRepository.findByIdAndNotDeleted(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        Tag tag = tagService.getTagsByIds(Set.of(tagId)).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + tagId));

        task.getTags().add(tag);
        getCurrentUserId().ifPresent(task::setModifiedBy);
        Task updatedTask = taskRepository.save(task);

        log.info("Tag {} added to task {} successfully", tagId, taskId);
        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    @Timed(value = "taskr.task.removeTag", description = "Time taken to remove a tag from a task")
    public TaskResponseDTO removeTagFromTask(Long taskId, Long tagId) {
        log.info("Removing tag {} from task {}", tagId, taskId);

        Task task = taskRepository.findByIdAndNotDeleted(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.getTags().removeIf(tag -> tag.getId().equals(tagId));
        getCurrentUserId().ifPresent(task::setModifiedBy);
        Task updatedTask = taskRepository.save(task);

        log.info("Tag {} removed from task {} successfully", tagId, taskId);
        return taskMapper.toDto(updatedTask);
    }
}
