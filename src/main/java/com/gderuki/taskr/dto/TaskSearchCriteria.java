package com.gderuki.taskr.dto;

import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Search criteria for filtering tasks")
public class TaskSearchCriteria {

    @Schema(description = "Search in title and description", example = "documentation")
    private String keyword;

    @Schema(description = "Filter by task status", example = "TODO")
    private TaskStatus status;

    @Schema(description = "Filter by task priority", example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "Filter by assignee user ID", example = "1")
    private Long assigneeId;

    @Schema(description = "Filter tasks with due date after this timestamp", example = "2026-01-01T00:00:00")
    private LocalDateTime dueDateFrom;

    @Schema(description = "Filter tasks with due date before this timestamp", example = "2026-12-31T23:59:59")
    private LocalDateTime dueDateTo;

    @Schema(description = "Filter tasks created after this timestamp", example = "2026-01-01T00:00:00")
    private LocalDateTime createdAfter;

    @Schema(description = "Filter tasks created before this timestamp", example = "2026-12-31T23:59:59")
    private LocalDateTime createdBefore;

    @Schema(description = "Include only unassigned tasks", example = "false")
    private Boolean unassignedOnly;

    @Schema(description = "Include only overdue tasks (due date in the past and status not DONE)", example = "false")
    private Boolean overdueOnly;

    @Schema(description = "Filter by tag IDs (tasks must have ALL specified tags)", example = "[1, 2]")
    private List<Long> tagIds;

    @Schema(description = "Filter by tag IDs (tasks must have ANY of specified tags)", example = "[1, 2]")
    private List<Long> anyTagIds;
}
