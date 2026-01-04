package com.gderuki.taskr.service;

import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduled service to monitor task due dates and send notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskDueDateScheduler {

    private final TaskRepository taskRepository;
    private final NotificationServiceInterface notificationService;

    @PostConstruct
    public void init() {
        log.info("TaskDueDateScheduler initialized with notification service: {}",
                notificationService.getClass().getSimpleName());
        log.info("Scheduler will check for upcoming tasks every hour and overdue tasks every 6 hours");
    }

    /**
     * Check for tasks due within the next 24 hours
     * Runs every hour (3,600,000 milliseconds)
     */
    @Scheduled(fixedRate = 3_600_000)
    @Transactional(readOnly = true)
    public void checkUpcomingDueDates() {
        log.debug("Running scheduled check for upcoming due dates");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);

        List<Task> upcomingTasks = taskRepository.findTasksWithDueDateBetween(now, next24Hours);

        log.info("Found {} tasks due in the next 24 hours", upcomingTasks.size());

        for (Task task : upcomingTasks) {
            long hoursUntilDue = ChronoUnit.HOURS.between(now, task.getDueDate());
            notificationService.sendDueDateNotification(task, hoursUntilDue);
        }
    }

    /**
     * Check for overdue tasks
     * Runs every 6 hours (21, 600, 000 milliseconds)
     */
    @Scheduled(fixedRate = 21_600_000)
    @Transactional(readOnly = true)
    public void checkOverdueTasks() {
        log.debug("Running scheduled check for overdue tasks");

        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = taskRepository.findOverdueTasks(now);

        log.info("Found {} overdue tasks", overdueTasks.size());

        for (Task task : overdueTasks) {
            notificationService.sendOverdueNotification(task);
        }
    }
}
