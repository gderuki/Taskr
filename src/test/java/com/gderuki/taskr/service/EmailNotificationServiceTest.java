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

class EmailNotificationServiceTest {

    private EmailNotificationService emailNotificationService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        emailNotificationService = new EmailNotificationService();
        logger = (Logger) LoggerFactory.getLogger(EmailNotificationService.class); // capture log messages
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void sendDueDateNotification_WithAssignedTask_ShouldLogInfoAndWarningWithEmailBody() {
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
        emailNotificationService.sendDueDateNotification(task, 24);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(2);

        ILoggingEvent firstLog = logsList.getFirst();
        assertThat(firstLog.getLevel()).isEqualTo(Level.WARN);
        assertThat(firstLog.getFormattedMessage())
                .contains("Sending email notification")
                .contains("Complete project report")
                .contains("ID: 1")
                .contains("2026-01-10 14:30")
                .contains("john.doe")
                .contains("john@example.com");

        ILoggingEvent warnLog = logsList.get(1);
        assertThat(warnLog.getLevel()).isEqualTo(Level.WARN);
        assertThat(warnLog.getFormattedMessage())
                .contains("<h2>Task Due Soon</h2>")
                .contains("Complete project report")
                .contains("24 hours")
                .contains("HIGH")
                .contains("IN_PROGRESS");
    }

    @Test
    void sendDueDateNotification_WithUnassignedTask_ShouldLogWithNullEmail() {
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
        emailNotificationService.sendDueDateNotification(task, 48);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(2);

        ILoggingEvent firstLog = logsList.getFirst();
        assertThat(firstLog.getFormattedMessage())
                .contains("Sending email notification")
                .contains("Review documentation")
                .contains("2026-01-05 09:00")
                .contains("Unassigned")
                .contains("(null)");
        ILoggingEvent emailBodyLog = logsList.get(1);
        assertThat(emailBodyLog.getFormattedMessage())
                .contains("48 hours");
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
        emailNotificationService.sendDueDateNotification(task, 0);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(2);
        assertThat(logsList.getFirst().getFormattedMessage())
                .contains("N/A")
                .contains("Unassigned");
    }

    @Test
    void sendDueDateNotification_WithZeroHours_ShouldIncludeZeroInEmailBody() {
        // Given
        User assignee = User.builder()
                .id(1L)
                .username("user")
                .email("user@example.com")
                .build();

        Task task = Task.builder()
                .id(4L)
                .title("Urgent task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assignee(assignee)
                .dueDate(LocalDateTime.now())
                .build();

        // When
        emailNotificationService.sendDueDateNotification(task, 0);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(2);
        ILoggingEvent warnLog = logsList.get(1);
        assertThat(warnLog.getFormattedMessage())
                .contains("0 hours");
    }

    @Test
    void sendOverdueNotification_WithAssignedTask_ShouldLogWarningAndCalculateHoursUntilDue() {
        // Given
        User assignee = User.builder()
                .id(1L)
                .username("jane.smith")
                .email("jane@example.com")
                .build();

        LocalDateTime pastDueDate = LocalDateTime.now().minusHours(10);
        Task task = Task.builder()
                .id(5L)
                .title("Submit tax forms")
                .description("Annual tax submission")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .assignee(assignee)
                .dueDate(pastDueDate)
                .build();

        // When
        emailNotificationService.sendOverdueNotification(task);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(2);

        ILoggingEvent warnLog = logsList.getFirst();
        assertThat(warnLog.getLevel()).isEqualTo(Level.WARN);
        assertThat(warnLog.getFormattedMessage())
                .contains("Sending overdue email notification")
                .contains("Submit tax forms")
                .contains("ID: 5")
                .contains("jane@example.com");

        ILoggingEvent emailBodyLog = logsList.get(1);
        assertThat(emailBodyLog.getLevel()).isEqualTo(Level.WARN);
        assertThat(emailBodyLog.getFormattedMessage())
                .contains("<h2>Task Due Soon</h2>")
                .contains("Submit tax forms")
                .contains("HIGH")
                .contains("IN_PROGRESS");

        String formattedMessage = emailBodyLog.getFormattedMessage();
        assertThat(formattedMessage).containsPattern("-1[0-9] hours"); // negative hours -> overdue
    }

    @Test
    void sendOverdueNotification_WithUnassignedTask_ShouldLogWithNullEmail() {
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
        emailNotificationService.sendOverdueNotification(task);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(2);

        ILoggingEvent warnLog = logsList.getFirst();
        assertThat(warnLog.getFormattedMessage())
                .contains("Sending overdue email notification")
                .contains("Clean up old files")
                .contains("null");
    }

    @Test
    void sendOverdueNotification_WithNullDueDate_ShouldUseZeroHours() {
        // Given
        Task task = Task.builder()
                .id(7L)
                .title("Overdue task without due date")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(null)
                .build();

        // When
        emailNotificationService.sendOverdueNotification(task);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(2);
        ILoggingEvent emailBodyLog = logsList.get(1);
        assertThat(emailBodyLog.getFormattedMessage())
                .contains("0 hours");
    }

    @Test
    void sendOverdueNotification_WithFutureDueDate_ShouldCalculatePositiveHours() {
        // Given
        LocalDateTime futureDueDate = LocalDateTime.now().plusHours(5);
        Task task = Task.builder()
                .id(8L)
                .title("Not actually overdue")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(futureDueDate)
                .build();

        // When
        emailNotificationService.sendOverdueNotification(task);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(2);
        ILoggingEvent emailBodyLog = logsList.get(1);
        String formattedMessage = emailBodyLog.getFormattedMessage();
        assertThat(formattedMessage).containsPattern("[4-5] hours");
    }

    @Test
    void sendDueDateNotification_ShouldFormatDateTimeCorrectly() {
        // Given
        User assignee = User.builder()
                .id(1L)
                .username("test.user")
                .email("test@example.com")
                .build();

        Task task = Task.builder()
                .id(9L)
                .title("Test task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .assignee(assignee)
                .dueDate(LocalDateTime.of(2026, 3, 15, 8, 45))
                .build();

        // When
        emailNotificationService.sendDueDateNotification(task, 100);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(2);
        assertThat(logsList.getFirst().getFormattedMessage())
                .contains("Test task");
    }
}
