package com.gderuki.taskr.dto;

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
@Schema(description = "Comment response")
public class CommentResponseDTO {

    @Schema(description = "Comment unique identifier", example = "1")
    private Long id;

    @Schema(description = "Comment content", example = "This task needs to be completed by end of week")
    private String content;

    @Schema(description = "Task ID this comment belongs to", example = "1")
    private Long taskId;

    @Schema(description = "Author user ID", example = "1")
    private Long authorId;

    @Schema(description = "Author username", example = "john.doe")
    private String authorUsername;

    @Schema(description = "Comment creation timestamp", example = "2026-01-04T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Comment last update timestamp", example = "2026-01-04T10:15:30")
    private LocalDateTime updatedAt;
}
