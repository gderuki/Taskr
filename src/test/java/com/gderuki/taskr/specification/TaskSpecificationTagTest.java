package com.gderuki.taskr.specification;

import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.dto.TaskSearchCriteria;
import com.gderuki.taskr.entity.Tag;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.repository.TagRepository;
import com.gderuki.taskr.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TaskSpecification Tag Filtering Integration Tests")
class TaskSpecificationTagTest extends WithTestContainer {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TagRepository tagRepository;

    private Tag bugTag;
    private Tag featureTag;
    private Tag urgentTag;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        String testId = String.valueOf(System.currentTimeMillis());

        bugTag = tagRepository.save(Tag.builder()
                .name("TestBug_" + testId)
                .color("#FF0000")
                .build());

        featureTag = tagRepository.save(Tag.builder()
                .name("TestFeature_" + testId)
                .color("#00FF00")
                .build());

        urgentTag = tagRepository.save(Tag.builder()
                .name("TestUrgent_" + testId)
                .color("#FFA500")
                .build());

        task1 = taskRepository.save(Task.builder()
                .title("Fix login bug")
                .description("Users cannot login")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .tags(new HashSet<>(Set.of(bugTag)))
                .build());

        task2 = taskRepository.save(Task.builder()
                .title("Add new feature with bug fix")
                .description("Implement feature X and fix related bug")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .tags(new HashSet<>(Set.of(bugTag, featureTag)))
                .build());

        task3 = taskRepository.save(Task.builder()
                .title("Urgent feature")
                .description("Critical feature needed ASAP")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.URGENT)
                .tags(new HashSet<>(Set.of(featureTag, urgentTag)))
                .build());
    }

    @Test
    @DisplayName("Should find tasks with ANY of the specified tags")
    void shouldFindTasksWithAnyTag() {
        // Given
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .anyTagIds(List.of(bugTag.getId(), featureTag.getId()))
                .build();

        Specification<Task> spec = TaskSpecification.withCriteria(criteria);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Task> result = taskRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(Task::getId)
                .containsExactlyInAnyOrder(task1.getId(), task2.getId(), task3.getId());
    }

    @Test
    @DisplayName("Should find tasks with ALL specified tags")
    void shouldFindTasksWithAllTags() {
        // Given
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .tagIds(List.of(bugTag.getId(), featureTag.getId()))
                .build();

        Specification<Task> spec = TaskSpecification.withCriteria(criteria);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Task> result = taskRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(task2.getId());
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Add new feature with bug fix");
    }

    @Test
    @DisplayName("Should find tasks with specific single tag using ANY filter")
    void shouldFindTasksWithSingleTag() {
        // Given
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .anyTagIds(List.of(urgentTag.getId()))
                .build();

        Specification<Task> spec = TaskSpecification.withCriteria(criteria);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Task> result = taskRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(task3.getId());
    }

    @Test
    @DisplayName("Should return empty when no tasks have ALL required tags")
    void shouldReturnEmptyWhenNoTasksHaveAllTags() {
        // Given
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .tagIds(List.of(bugTag.getId(), urgentTag.getId()))
                .build();

        Specification<Task> spec = TaskSpecification.withCriteria(criteria);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Task> result = taskRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should combine tag filter with other criteria")
    void shouldCombineTagFilterWithOtherCriteria() {
        // Given
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .status(TaskStatus.TODO)
                .anyTagIds(List.of(featureTag.getId()))
                .build();

        Specification<Task> spec = TaskSpecification.withCriteria(criteria);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Task> result = taskRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(task3.getId());
    }

    @Test
    @DisplayName("Should not return deleted tasks with tag filter")
    void shouldNotReturnDeletedTasksWithTagFilter() {
        // Given
        task1.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task1);

        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .anyTagIds(List.of(bugTag.getId()))
                .build();

        Specification<Task> spec = TaskSpecification.withCriteria(criteria);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Task> result = taskRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(task2.getId());
    }

    @Test
    @DisplayName("Should return all tasks when no tag filter is provided")
    void shouldReturnAllTasksWithoutTagFilter() {
        // Given
        TaskSearchCriteria criteria = TaskSearchCriteria.builder().build();

        Specification<Task> spec = TaskSpecification.withCriteria(criteria);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Task> result = taskRepository.findAll(spec, pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
    }
}
