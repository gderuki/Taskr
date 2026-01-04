package com.gderuki.taskr.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {

    private ConsoleNotificationService notificationService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        notificationService = new ConsoleNotificationService();
        logger = (Logger) LoggerFactory.getLogger(ConsoleNotificationService.class); // capture log messages
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void sendDueDateNotification_WithAssignedTask_ShouldLogWarningWithAllDetails() {
        // Given
        User assignee = User.builder()
                .id(1L)
                .username("john.doe")
                .email("john@example.com")
                .build();

        Task task = Task.builder()
                .id(1L)
                .title("Complete project report")
                .description("Q4 project report")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .assignee(assignee)
                .dueDate(LocalDateTime.of(2026, 1, 10, 14, 30))
                .build();

        // When
        notificationService.sendDueDateNotification(task, 24);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        ILoggingEvent logEvent = logsList.getFirst();
        assertThat(logEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(logEvent.getFormattedMessage())
                .contains("[!] TASK DUE SOON:")
                .contains("Complete project report")
                .contains("ID: 1")
                .contains("24 hours")
                .contains("2026-01-10 14:30")
                .contains("john.doe");
    }

    @Test
    void sendDueDateNotification_WithUnassignedTask_ShouldLogWithUnassignedLabel() {
        // Given
        Task task = Task.builder()
                .id(2L)
                .title("Review documentation")
                .description("Review API docs")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .assignee(null)
                .dueDate(LocalDateTime.of(2026, 1, 5, 9, 0))
                .build();

        // When
        notificationService.sendDueDateNotification(task, 48);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        ILoggingEvent logEvent = logsList.getFirst();
        assertThat(logEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(logEvent.getFormattedMessage())
                .contains("[!] TASK DUE SOON:")
                .contains("Review documentation")
                .contains("ID: 2")
                .contains("48 hours")
                .contains("Unassigned");
    }

    @Test
    void sendDueDateNotification_WithNullDueDate_ShouldLogNA() {
        // Given
        Task task = Task.builder()
                .id(3L)
                .title("Task without due date")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .dueDate(null)
                .build();

        // When
        notificationService.sendDueDateNotification(task, 0);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        ILoggingEvent logEvent = logsList.getFirst();
        assertThat(logEvent.getFormattedMessage())
                .contains("N/A");
    }

    @Test
    void sendOverdueNotification_WithAssignedTask_ShouldLogErrorWithAllDetails() {
        // Given
        User assignee = User.builder()
                .id(1L)
                .username("jane.smith")
                .email("jane@example.com")
                .build();

        Task task = Task.builder()
                .id(5L)
                .title("Submit tax forms")
                .description("Annual tax submission")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .assignee(assignee)
                .dueDate(LocalDateTime.of(2026, 1, 1, 23, 59))
                .build();

        // When
        notificationService.sendOverdueNotification(task);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        ILoggingEvent logEvent = logsList.getFirst();
        assertThat(logEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(logEvent.getFormattedMessage())
                .contains("[!!!] TASK OVERDUE:")
                .contains("Submit tax forms")
                .contains("ID: 5")
                .contains("2026-01-01 23:59")
                .contains("jane.smith");
    }

    @Test
    void sendOverdueNotification_WithUnassignedTask_ShouldLogWithUnassignedLabel() {
        // Given
        Task task = Task.builder()
                .id(6L)
                .title("Clean up old files")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .assignee(null)
                .dueDate(LocalDateTime.of(2025, 12, 31, 12, 0))
                .build();

        // When
        notificationService.sendOverdueNotification(task);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        ILoggingEvent logEvent = logsList.getFirst();
        assertThat(logEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(logEvent.getFormattedMessage())
                .contains("[!!!] TASK OVERDUE:")
                .contains("Clean up old files")
                .contains("ID: 6")
                .contains("Unassigned");
    }

    @Test
    void sendOverdueNotification_WithNullDueDate_ShouldLogNA() {
        // Given
        Task task = Task.builder()
                .id(7L)
                .title("Overdue task without due date")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(null)
                .build();

        // When
        notificationService.sendOverdueNotification(task);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        ILoggingEvent logEvent = logsList.getFirst();
        assertThat(logEvent.getFormattedMessage())
                .contains("N/A");
    }

    @Test
    void sendDueDateNotification_WithZeroHoursUntilDue_ShouldLogCorrectly() {
        // Given
        Task task = Task.builder()
                .id(8L)
                .title("Urgent task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDateTime.now())
                .build();

        // When
        notificationService.sendDueDateNotification(task, 0);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        ILoggingEvent logEvent = logsList.getFirst();
        assertThat(logEvent.getFormattedMessage())
                .contains("0 hours");
    }
}
