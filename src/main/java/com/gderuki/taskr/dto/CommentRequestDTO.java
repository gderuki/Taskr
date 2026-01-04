package com.gderuki.taskr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Comment creation/update request")
public class CommentRequestDTO {

    @Schema(description = "Comment content", example = "This task needs to be completed by end of week", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    private String content;
}
