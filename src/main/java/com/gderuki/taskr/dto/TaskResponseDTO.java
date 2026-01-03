package com.gderuki.taskr.dto;

import com.gderuki.taskr.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Task response")
public class TaskResponseDTO {

    @Schema(description = "Task unique identifier", example = "1")
    private Long id;

    @Schema(description = "Task title", example = "Complete project documentation")
    private String title;

    @Schema(description = "Task description", example = "Write comprehensive API documentation using SpringDoc OpenAPI")
    private String description;

    @Schema(description = "Task status", example = "TODO")
    private TaskStatus status;

    @Schema(description = "Task creation timestamp", example = "2026-01-03T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Task last update timestamp", example = "2026-01-03T10:15:30")
    private LocalDateTime updatedAt;
}
