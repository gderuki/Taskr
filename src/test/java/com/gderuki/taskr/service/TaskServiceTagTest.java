package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.dto.TaskResponseDTO;
import com.gderuki.taskr.entity.Tag;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.exception.TaskNotFoundException;
import com.gderuki.taskr.mapper.TaskMapper;
import com.gderuki.taskr.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Tag Operations Tests")
class TaskServiceTagTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TagService tagService;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRequestDTO taskRequestDTO;
    private TaskResponseDTO taskResponseDTO;
    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        tag1 = Tag.builder()
                .id(1L)
                .name("Bug")
                .color("#FF0000")
                .build();

        tag2 = Tag.builder()
                .id(2L)
                .name("Feature")
                .color("#00FF00")
                .build();

        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .tags(new HashSet<>())
                .build();

        taskRequestDTO = TaskRequestDTO.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .tagIds(Set.of(1L, 2L))
                .build();

        taskResponseDTO = TaskResponseDTO.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create task with tags")
    void shouldCreateTaskWithTags() {
        // Given
        Set<Tag> tags = Set.of(tag1, tag2);
        when(taskMapper.toEntity(taskRequestDTO)).thenReturn(task);
        when(tagService.getTagsByIds(taskRequestDTO.getTagIds())).thenReturn(tags);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        // When
        TaskResponseDTO result = taskService.createTask(taskRequestDTO);

        // Then
        assertThat(result).isNotNull();
        verify(tagService).getTagsByIds(taskRequestDTO.getTagIds());
        verify(taskRepository).save(task);
        assertThat(task.getTags()).isEqualTo(tags);
    }

    @Test
    @DisplayName("Should update task with tags")
    void shouldUpdateTaskWithTags() {
        // Given
        Set<Tag> newTags = Set.of(tag1);
        taskRequestDTO.setTagIds(Set.of(1L));

        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(tagService.getTagsByIds(taskRequestDTO.getTagIds())).thenReturn(newTags);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        // When
        TaskResponseDTO result = taskService.updateTask(1L, taskRequestDTO);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findByIdAndNotDeleted(1L);
        verify(tagService).getTagsByIds(taskRequestDTO.getTagIds());
        verify(taskRepository).save(task);
        assertThat(task.getTags()).isEqualTo(newTags);
    }

    @Test
    @DisplayName("Should clear tags when updating with empty tag list")
    void shouldClearTagsWhenUpdatingWithEmptyTagList() {
        // Given
        task.getTags().add(tag1);
        taskRequestDTO.setTagIds(new HashSet<>());

        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        // When
        TaskResponseDTO result = taskService.updateTask(1L, taskRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(task.getTags()).isEmpty();
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Should add tag to task")
    void shouldAddTagToTask() {
        // Given
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(tagService.getTagsByIds(Set.of(1L))).thenReturn(Set.of(tag1));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        // When
        TaskResponseDTO result = taskService.addTagToTask(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findByIdAndNotDeleted(1L);
        verify(tagService).getTagsByIds(Set.of(1L));
        verify(taskRepository).save(task);
        assertThat(task.getTags()).contains(tag1);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when adding tag to non-existent task")
    void shouldThrowTaskNotFoundExceptionWhenAddingTagToNonExistentTask() {
        // Given
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.addTagToTask(1L, 1L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("1");
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should remove tag from task")
    void shouldRemoveTagFromTask() {
        // Given
        task.getTags().add(tag1);
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        // When
        TaskResponseDTO result = taskService.removeTagFromTask(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(taskRepository).findByIdAndNotDeleted(1L);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when removing tag from non-existent task")
    void shouldThrowTaskNotFoundExceptionWhenRemovingTagFromNonExistentTask() {
        // Given
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.removeTagFromTask(1L, 1L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("1");
        verify(taskRepository, never()).save(any());
    }
}
