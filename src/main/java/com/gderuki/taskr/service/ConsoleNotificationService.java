package com.gderuki.taskr.service;

import com.gderuki.taskr.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@ConditionalOnProperty(
        name = "app.notification.type",
        havingValue = "console",
        matchIfMissing = true
)
@Slf4j
public class ConsoleNotificationService implements NotificationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void sendDueDateNotification(Task task, long hoursUntilDue) {
        String assigneeName = task.getAssignee() != null
                ? task.getAssignee().getUsername()
                : "Unassigned";

        String dueDate = task.getDueDate() != null
                ? task.getDueDate().format(FORMATTER)
                : "N/A";

        log.warn("[!] TASK DUE SOON: '{}' (ID: {}) is due in {} hours at {}. Assigned to: {}",
                task.getTitle(),
                task.getId(),
                hoursUntilDue,
                dueDate,
                assigneeName);

        // TODO: Implement actual notification mechanism
    }

    @Override
    public void sendOverdueNotification(Task task) {
        String assigneeName = task.getAssignee() != null
                ? task.getAssignee().getUsername()
                : "Unassigned";

        String dueDate = task.getDueDate() != null
                ? task.getDueDate().format(FORMATTER)
                : "N/A";

        log.error("[!!!] TASK OVERDUE: '{}' (ID: {}) was due at {}. Assigned to: {}",
                task.getTitle(),
                task.getId(),
                dueDate,
                assigneeName);

        // TODO: Implement actual notification mechanism
    }
}
