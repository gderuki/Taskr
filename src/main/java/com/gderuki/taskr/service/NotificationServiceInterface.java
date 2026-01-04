package com.gderuki.taskr.service;

import com.gderuki.taskr.entity.Task;

/**
 * Interface for handling task notifications
 * <p>
 * Implementations can send notifications via different channels:
 * - Console/Logging (default)
 * - Email
 * - SMS
 * - Push notifications
 * - Multiple channels (composite)
 */
public interface NotificationServiceInterface {

    /**
     * Send notification for a task that is due soon
     *
     * @param task The task that is due soon
     * @param hoursUntilDue Number of hours until the task is due
     */
    void sendDueDateNotification(Task task, long hoursUntilDue);

    /**
     * Send a notification for a task that is overdue
     *
     * @param task The overdue task
     */
    void sendOverdueNotification(Task task);
}
