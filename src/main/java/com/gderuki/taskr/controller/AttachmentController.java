package com.gderuki.taskr.controller;

import com.gderuki.taskr.config.ApiConstants;
import com.gderuki.taskr.dto.AttachmentResponseDTO;
import com.gderuki.taskr.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.Tasks.BASE + "/{taskId}/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "File attachment management APIs for tasks")
@SecurityRequirement(name = "Bearer Authentication")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(
            summary = "Upload attachment to task",
            description = "Upload a file attachment to a specific task. Supports multiple file types."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Attachment uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AttachmentResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "File storage error")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponseDTO> uploadAttachment(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file) {

        AttachmentResponseDTO response = attachmentService.uploadAttachment(taskId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all attachments for a task",
            description = "Retrieve all file attachments associated with a specific task"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attachments retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AttachmentResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping
    public ResponseEntity<List<AttachmentResponseDTO>> getAttachments(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long taskId) {

        List<AttachmentResponseDTO> attachments = attachmentService.getAttachmentsByTaskId(taskId);
        return ResponseEntity.ok(attachments);
    }

    @Operation(
            summary = "Get attachment metadata",
            description = "Retrieve metadata for a specific attachment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attachment metadata retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AttachmentResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Task or attachment not found")
    })
    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponseDTO> getAttachment(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Attachment ID", required = true)
            @PathVariable Long attachmentId) {

        AttachmentResponseDTO attachment = attachmentService.getAttachment(taskId, attachmentId);
        return ResponseEntity.ok(attachment);
    }

    @Operation(
            summary = "Download attachment",
            description = "Download a specific file attachment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attachment downloaded successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ),
            @ApiResponse(responseCode = "404", description = "Task or attachment not found"),
            @ApiResponse(responseCode = "500", description = "File storage error")
    })
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Attachment ID", required = true)
            @PathVariable Long attachmentId) {

        Resource resource = attachmentService.downloadAttachment(taskId, attachmentId);
        AttachmentResponseDTO metadata = attachmentService.getAttachment(taskId, attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalFileName() + "\"")
                .body(resource);
    }

    @Operation(
            summary = "Delete attachment",
            description = "Delete a specific file attachment (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task or attachment not found")
    })
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Attachment ID", required = true)
            @PathVariable Long attachmentId) {

        attachmentService.deleteAttachment(taskId, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Count attachments",
            description = "Get the count of attachments for a specific task"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attachment count retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/count")
    public ResponseEntity<Long> countAttachments(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long taskId) {

        long count = attachmentService.countAttachmentsByTaskId(taskId);
        return ResponseEntity.ok(count);
    }
}
