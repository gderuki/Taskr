package com.gderuki.taskr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponseDTO {

    private Long id;
    private String fileName;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String storageProvider;
    private Long taskId;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;
}
