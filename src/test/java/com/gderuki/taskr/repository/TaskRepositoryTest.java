package com.gderuki.taskr.repository;

import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskRepositoryTest extends WithTestContainer {

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Nested
    class FindAllActiveTests {

        @Test
        void shouldReturnOnlyNonDeletedTasks() {
            Task activeTask = Task.builder()
                    .title("Active Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .build();

            Task deletedTask = Task.builder()
                    .title("Deleted Task")
                    .status(TaskStatus.DONE)
                    .priority(TaskPriority.LOW)
                    .deletedAt(LocalDateTime.now())
                    .build();

            taskRepository.save(activeTask);
            taskRepository.save(deletedTask);

            Page<Task> result = taskRepository.findAllActive(PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Active Task");
        }

        @Test
        void shouldReturnCorrectPageWithPagination() {
            for (int i = 1; i <= 5; i++) {
                Task task = Task.builder()
                        .title("Task " + i)
                        .status(TaskStatus.TODO)
                        .priority(TaskPriority.MEDIUM)
                        .build();
                taskRepository.save(task);
            }

            Page<Task> firstPage = taskRepository.findAllActive(PageRequest.of(0, 2));
            assertThat(firstPage.getContent()).hasSize(2);
            assertThat(firstPage.getTotalElements()).isEqualTo(5);
            assertThat(firstPage.getTotalPages()).isEqualTo(3);
            assertThat(firstPage.hasNext()).isTrue();

            Page<Task> secondPage = taskRepository.findAllActive(PageRequest.of(1, 2));
            assertThat(secondPage.getContent()).hasSize(2);
            assertThat(secondPage.hasNext()).isTrue();

            Page<Task> lastPage = taskRepository.findAllActive(PageRequest.of(2, 2));
            assertThat(lastPage.getContent()).hasSize(1);
            assertThat(lastPage.hasNext()).isFalse();
        }

        @Test
        void shouldReturnEmptyPageWhenNoTasks() {
            Page<Task> result = taskRepository.findAllActive(PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
        }

        @Test
        void shouldReturnSortedTasksByTitle() {
            Task taskB = Task.builder()
                    .title("B Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .build();
            Task taskA = Task.builder()
                    .title("A Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .build();
            Task taskC = Task.builder()
                    .title("C Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .build();

            taskRepository.save(taskB);
            taskRepository.save(taskA);
            taskRepository.save(taskC);

            Page<Task> ascending = taskRepository.findAllActive(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"))
            );
            List<String> ascTitles = ascending.getContent().stream()
                    .map(Task::getTitle)
                    .toList();
            assertThat(ascTitles).containsExactly("A Task", "B Task", "C Task");

            Page<Task> descending = taskRepository.findAllActive(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "title"))
            );
            List<String> descTitles = descending.getContent().stream()
                    .map(Task::getTitle)
                    .toList();
            assertThat(descTitles).containsExactly("C Task", "B Task", "A Task");
        }

        @Test
        void shouldReturnSortedTasksByStatus() {
            Task todoTask = Task.builder()
                    .title("Todo Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .build();
            Task inProgressTask = Task.builder()
                    .title("In Progress Task")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.HIGH)
                    .build();
            Task doneTask = Task.builder()
                    .title("Done Task")
                    .status(TaskStatus.DONE)
                    .priority(TaskPriority.LOW)
                    .build();

            taskRepository.save(doneTask);
            taskRepository.save(todoTask);
            taskRepository.save(inProgressTask);

            Page<Task> result = taskRepository.findAllActive(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "status"))
            );

            List<TaskStatus> statuses = result.getContent().stream()
                    .map(Task::getStatus)
                    .toList();
            assertThat(statuses).containsExactly(TaskStatus.DONE, TaskStatus.IN_PROGRESS, TaskStatus.TODO);
        }

        @Test
        void shouldWorkWithMultipleSortFields() {
            Task task1 = Task.builder()
                    .title("A Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .build();
            Task task2 = Task.builder()
                    .title("B Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .build();
            Task task3 = Task.builder()
                    .title("C Task")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.MEDIUM)
                    .build();

            taskRepository.save(task2);
            taskRepository.save(task3);
            taskRepository.save(task1);

            Page<Task> result = taskRepository.findAllActive(
                    PageRequest.of(0, 10, Sort.by(
                            Sort.Order.desc("status"),
                            Sort.Order.asc("title")
                    ))
            );

            List<String> titles = result.getContent().stream()
                    .map(Task::getTitle)
                    .toList();
            assertThat(titles).containsExactly("A Task", "B Task", "C Task");
        }

        @Test
        void shouldOnlyReturnActiveFromMixedTasks() {
            for (int i = 1; i <= 10; i++) {
                Task task = Task.builder()
                        .title("Task " + i)
                        .status(TaskStatus.TODO)
                        .priority(TaskPriority.MEDIUM)
                        .deletedAt(i % 2 == 0 ? LocalDateTime.now() : null)
                        .build();
                taskRepository.save(task);
            }

            Page<Task> result = taskRepository.findAllActive(PageRequest.of(0, 20));
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getContent()).allMatch(task -> task.getDeletedAt() == null);
        }
    }

    @Nested
    class FindByIdAndNotDeletedTests {

        @Test
        void shouldReturnTaskWhenActive() {
            Task task = Task.builder()
                    .title("Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .build();
            Task saved = taskRepository.save(task);

            Optional<Task> found = taskRepository.findByIdAndNotDeleted(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Task");
        }

        @Test
        void shouldReturnEmptyWhenDeleted() {
            Task task = Task.builder()
                    .title("Deleted Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .deletedAt(LocalDateTime.now())
                    .build();
            Task saved = taskRepository.save(task);

            Optional<Task> found = taskRepository.findByIdAndNotDeleted(saved.getId());
            assertThat(found).isEmpty();
        }

        @Test
        void shouldReturnEmptyWithNullId() {
            Optional<Task> found = taskRepository.findByIdAndNotDeleted(null);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    class ExistsByIdAndNotDeletedTests {

        @Test
        void shouldWorkCorrectly() {
            Task activeTask = Task.builder()
                    .title("Active Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .build();
            Task deletedTask = Task.builder()
                    .title("Deleted Task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .deletedAt(LocalDateTime.now())
                    .build();

            Task savedActive = taskRepository.save(activeTask);
            Task savedDeleted = taskRepository.save(deletedTask);

            assertThat(taskRepository.existsByIdAndNotDeleted(savedActive.getId())).isTrue();
            assertThat(taskRepository.existsByIdAndNotDeleted(savedDeleted.getId())).isFalse();
            assertThat(taskRepository.existsByIdAndNotDeleted(999L)).isFalse();
        }
    }

    @Nested
    class DueDateTests {

        @Test
        void findTasksWithDueDateBetween_shouldReturnTasksInTimeRange() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future24Hours = now.plusHours(24);

            Task taskInRange = Task.builder()
                    .title("Task in range")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(now.plusHours(12))
                    .build();

            Task taskOutOfRange = Task.builder()
                    .title("Task out of range")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.LOW)
                    .dueDate(now.plusHours(48))
                    .build();

            taskRepository.save(taskInRange);
            taskRepository.save(taskOutOfRange);

            List<Task> result = taskRepository.findTasksWithDueDateBetween(now, future24Hours);
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTitle()).isEqualTo("Task in range");
        }

        @Test
        void findTasksWithDueDateBetween_shouldNotReturnDeletedTasks() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future24Hours = now.plusHours(24);

            Task activeTask = Task.builder()
                    .title("Active task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(now.plusHours(12))
                    .build();

            Task deletedTask = Task.builder()
                    .title("Deleted task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(now.plusHours(6))
                    .deletedAt(LocalDateTime.now())
                    .build();

            taskRepository.save(activeTask);
            taskRepository.save(deletedTask);

            List<Task> result = taskRepository.findTasksWithDueDateBetween(now, future24Hours);
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTitle()).isEqualTo("Active task");
        }

        @Test
        void findOverdueTasks_shouldReturnTasksWithPastDueDate() {
            LocalDateTime now = LocalDateTime.now();

            Task overdueTask = Task.builder()
                    .title("Overdue task")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.HIGH)
                    .dueDate(now.minusHours(6))
                    .build();

            Task futureTask = Task.builder()
                    .title("Future task")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(now.plusHours(6))
                    .build();

            taskRepository.save(overdueTask);
            taskRepository.save(futureTask);

            List<Task> result = taskRepository.findOverdueTasks(now);
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTitle()).isEqualTo("Overdue task");
        }

        @Test
        void findOverdueTasks_shouldNotReturnCompletedTasks() {
            LocalDateTime now = LocalDateTime.now();

            Task overdueIncomplete = Task.builder()
                    .title("Overdue incomplete")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .dueDate(now.minusHours(6))
                    .build();

            Task overdueComplete = Task.builder()
                    .title("Overdue complete")
                    .status(TaskStatus.DONE)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(now.minusHours(12))
                    .build();

            taskRepository.save(overdueIncomplete);
            taskRepository.save(overdueComplete);

            List<Task> result = taskRepository.findOverdueTasks(now);
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTitle()).isEqualTo("Overdue incomplete");
        }

        @Test
        void findOverdueTasks_shouldNotReturnDeletedTasks() {
            LocalDateTime now = LocalDateTime.now();

            Task overdueActive = Task.builder()
                    .title("Overdue active")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.HIGH)
                    .dueDate(now.minusHours(6))
                    .build();

            Task overdueDeleted = Task.builder()
                    .title("Overdue deleted")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(now.minusHours(12))
                    .deletedAt(LocalDateTime.now())
                    .build();

            taskRepository.save(overdueActive);
            taskRepository.save(overdueDeleted);

            List<Task> result = taskRepository.findOverdueTasks(now);
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTitle()).isEqualTo("Overdue active");
        }
    }
}
