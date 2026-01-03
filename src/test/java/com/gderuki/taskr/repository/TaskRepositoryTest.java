package com.gderuki.taskr.repository;

import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.entity.Task;
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
                    .build();

            Task deletedTask = Task.builder()
                    .title("Deleted Task")
                    .status(TaskStatus.DONE)
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
                    .build();
            Task taskA = Task.builder()
                    .title("A Task")
                    .status(TaskStatus.TODO)
                    .build();
            Task taskC = Task.builder()
                    .title("C Task")
                    .status(TaskStatus.TODO)
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
                    .build();
            Task inProgressTask = Task.builder()
                    .title("In Progress Task")
                    .status(TaskStatus.IN_PROGRESS)
                    .build();
            Task doneTask = Task.builder()
                    .title("Done Task")
                    .status(TaskStatus.DONE)
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
                    .build();
            Task task2 = Task.builder()
                    .title("B Task")
                    .status(TaskStatus.TODO)
                    .build();
            Task task3 = Task.builder()
                    .title("C Task")
                    .status(TaskStatus.IN_PROGRESS)
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
                    .build();
            Task deletedTask = Task.builder()
                    .title("Deleted Task")
                    .status(TaskStatus.TODO)
                    .deletedAt(LocalDateTime.now())
                    .build();

            Task savedActive = taskRepository.save(activeTask);
            Task savedDeleted = taskRepository.save(deletedTask);

            assertThat(taskRepository.existsByIdAndNotDeleted(savedActive.getId())).isTrue();
            assertThat(taskRepository.existsByIdAndNotDeleted(savedDeleted.getId())).isFalse();
            assertThat(taskRepository.existsByIdAndNotDeleted(999L)).isFalse();
        }
    }
}
