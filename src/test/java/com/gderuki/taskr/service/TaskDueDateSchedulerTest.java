package com.gderuki.taskr.service;

import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskDueDateSchedulerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskDueDateScheduler scheduler;

    private Task taskDueSoon;
    private Task taskOverdue;

    @BeforeEach
    void setUp() {
        taskDueSoon = Task.builder()
                .id(1L)
                .title("Task due soon")
                .description("Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDateTime.now().plusHours(12))
                .build();

        taskOverdue = Task.builder()
                .id(2L)
                .title("Overdue task")
                .description("Description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.URGENT)
                .dueDate(LocalDateTime.now().minusHours(6))
                .build();
    }

    @Test
    void checkUpcomingDueDates_shouldNotifyForTasksDueWithin24Hours() {
        // Given
        List<Task> upcomingTasks = Collections.singletonList(taskDueSoon);
        when(taskRepository.findTasksWithDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(upcomingTasks);

        // When
        scheduler.checkUpcomingDueDates();

        // Then
        verify(taskRepository).findTasksWithDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(notificationService).sendDueDateNotification(eq(taskDueSoon), anyLong());
    }

    @Test
    void checkUpcomingDueDates_shouldNotNotifyWhenNoTasksDue() {
        // Given
        when(taskRepository.findTasksWithDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        scheduler.checkUpcomingDueDates();

        // Then
        verify(taskRepository).findTasksWithDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(notificationService, never()).sendDueDateNotification(any(), anyLong());
    }

    @Test
    void checkOverdueTasks_shouldNotifyForOverdueTasks() {
        // Given
        List<Task> overdueTasks = Collections.singletonList(taskOverdue);
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class)))
                .thenReturn(overdueTasks);

        // When
        scheduler.checkOverdueTasks();

        // Then
        verify(taskRepository).findOverdueTasks(any(LocalDateTime.class));
        verify(notificationService).sendOverdueNotification(taskOverdue);
    }

    @Test
    void checkOverdueTasks_shouldNotNotifyWhenNoTasksOverdue() {
        // Given
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        scheduler.checkOverdueTasks();

        // Then
        verify(taskRepository).findOverdueTasks(any(LocalDateTime.class));
        verify(notificationService, never()).sendOverdueNotification(any());
    }

    @Test
    void checkUpcomingDueDates_shouldHandleMultipleTasks() {
        // Given
        Task task1 = Task.builder()
                .id(1L)
                .title("Task 1")
                .dueDate(LocalDateTime.now().plusHours(6))
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        Task task2 = Task.builder()
                .id(2L)
                .title("Task 2")
                .dueDate(LocalDateTime.now().plusHours(18))
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .build();

        List<Task> upcomingTasks = Arrays.asList(task1, task2);
        when(taskRepository.findTasksWithDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(upcomingTasks);

        // When
        scheduler.checkUpcomingDueDates();

        // Then
        verify(notificationService, times(2)).sendDueDateNotification(any(Task.class), anyLong());
    }
}
