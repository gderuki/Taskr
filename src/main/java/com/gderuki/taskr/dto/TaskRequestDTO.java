package com.gderuki.taskr.dto;

import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Task creation/update request")
public class TaskRequestDTO {

    @Schema(description = "Task title", example = "Complete project documentation", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    @Schema(description = "Task description", example = "Write comprehensive API documentation using SpringDoc OpenAPI", nullable = true)
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Schema(description = "Task status", example = "TODO", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"TODO", "IN_PROGRESS", "DONE"})
    @NotNull(message = "Status is required")
    private TaskStatus status;

    @Schema(description = "Task priority", example = "MEDIUM", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @Schema(description = "Assignee user ID (optional)", example = "1", nullable = true)
    private Long assigneeId;

    @Schema(description = "Task due date (optional)", example = "2031-01-15T17:00:00", nullable = true)
    private LocalDateTime dueDate;

    @Schema(description = "Tag IDs to associate with task (optional)", example = "[1, 2, 3]", nullable = true)
    private Set<Long> tagIds;
}
