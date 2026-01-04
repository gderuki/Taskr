package com.gderuki.taskr.service;

import com.gderuki.taskr.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@ConditionalOnProperty(
        name = "app.notification.type",
        havingValue = "email"
)
@Slf4j
public class EmailNotificationService implements NotificationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void sendDueDateNotification(Task task, long hoursUntilDue) {
        String assigneeName = task.getAssignee() != null
                ? task.getAssignee().getUsername()
                : "Unassigned";

        String assigneeEmail = task.getAssignee() != null
                ? task.getAssignee().getEmail()
                : null;

        String dueDate = task.getDueDate() != null
                ? task.getDueDate().format(FORMATTER)
                : "N/A";

        log.warn("Sending email notification: Task '{}' (ID: {}) was due at {} to {} ({})",
                task.getTitle(), task.getId(), dueDate, assigneeName, assigneeEmail);

        // TODO: Implement actual email sending
        log.warn(buildEmailBody(task, hoursUntilDue));
    }

    @Override
    public void sendOverdueNotification(Task task) {
        String assigneeName = task.getAssignee() != null
                ? task.getAssignee().getUsername()
                : "Unassigned";

        String assigneeEmail = task.getAssignee() != null
                ? task.getAssignee().getEmail()
                : null;

        String dueDate = task.getDueDate() != null
                ? task.getDueDate().format(FORMATTER)
                : "N/A";

        long hoursUntilDue = task.getDueDate() != null
                ? ChronoUnit.HOURS.between(LocalDateTime.now(), task.getDueDate())
                : 0;

        log.warn("Sending overdue email notification: Task '{}' (ID: {}) was due at {} to {} ({})",
                task.getTitle(), task.getId(), dueDate, assigneeName, assigneeEmail);

        // TODO: Implement actual email sending
        log.warn(buildEmailBody(task, hoursUntilDue));
    }

    private String buildEmailBody(Task task, long hoursUntilDue) {
        return String.format("""
                <h2>Task Due Soon</h2>
                <p><strong>Title:</strong> %s</p>
                <p><strong>Due in:</strong> %d hours</p>
                <p><strong>Priority:</strong> %s</p>
                <p><strong>Status:</strong> %s</p>
                """, task.getTitle(), hoursUntilDue, task.getPriority(), task.getStatus());
    }
}
