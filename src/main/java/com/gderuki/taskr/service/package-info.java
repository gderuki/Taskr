/**
 * Service layer containing business logic for the Taskr application.
 *
 * <h2>Notification Service Implementations</h2>
 *
 * The notification system uses interface-based design with configuration-driven selection.
 * Available implementations:
 *
 * <ul>
 *   <li><b>ConsoleNotificationService</b> - Console/logging (default)</li>
 *   <li><b>EmailNotificationService</b> - Email notifications</li>
 * </ul>
 *
 * <h3>Configuration-Based Selection (Recommended Approach)</h3>
 *
 * The active implementation is controlled by {@code app.notification.type} in application.yml:
 *
 * <pre>{@code
 * # application.yml
 * app:
 *   notification:
 *     type: console  # Options: console, email, slack
 * }</pre>
 *
 * Or via environment variable:
 * <pre>{@code
 * export NOTIFICATION_TYPE=email
 * java -jar taskr.jar
 * }</pre>
 *
 * <h3>Switching Implementations</h3>
 *
 * <b>Development (console logging):</b>
 * <pre>{@code
 * app:
 *   notification:
 *     type: console  # or omit - console is default
 * }</pre>
 *
 * <b>Production (email):</b>
 * <pre>{@code
 * app:
 *   notification:
 *     type: email
 * }</pre>
 *
 * Spring automatically selects and injects the correct implementation at startup using
 * {@code @ConditionalOnProperty}. No code changes needed!
 *
 * <h3>Creating Custom Implementations</h3>
 *
 * <pre>{@code
 * @Service
 * @ConditionalOnProperty(name = "app.notification.type", havingValue = "slack")
 * public class SlackNotificationService implements NotificationService {
 *     @Override
 *     public void sendDueDateNotification(Task task, long hoursUntilDue) {
 *         // Send to Slack channel
 *     }
 *
 *     @Override
 *     public void sendOverdueNotification(Task task) {
 *         // Send urgent alert to Slack
 *     }
 * }
 * }</pre>
 *
 * Then activate it:
 * <pre>{@code
 * app:
 *   notification:
 *     type: slack
 * }</pre>
 *
 * @author paul gderuki
 * @version 1.0
 */
package com.gderuki.taskr.service;
