package com.gderuki.taskr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tag creation/update request")
public class TagRequestDTO {

    @Schema(description = "Tag name", example = "Bug", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Tag name is required")
    @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters")
    private String name;

    @Schema(description = "Tag color in hex format", example = "#FF0000", nullable = true)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code (e.g., #FF0000)")
    private String color;
}
