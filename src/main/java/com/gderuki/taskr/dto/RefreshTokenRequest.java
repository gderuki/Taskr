package com.gderuki.taskr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token request")
public class RefreshTokenRequest {

    @Schema(description = "Refresh token", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
