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
@Schema(description = "Tag response")
public class TagDTO {

    @Schema(description = "Tag unique identifier", example = "1")
    private Long id;

    @Schema(description = "Tag name", example = "Bug")
    private String name;

    @Schema(description = "Tag color in hex format", example = "#FF0000", nullable = true)
    private String color;

    @Schema(description = "Tag creation timestamp", example = "2026-01-03T10:15:30")
    private LocalDateTime createdAt;
}
