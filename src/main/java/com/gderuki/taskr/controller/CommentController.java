package com.gderuki.taskr.controller;

import com.gderuki.taskr.config.ApiConstants;
import com.gderuki.taskr.dto.CommentRequestDTO;
import com.gderuki.taskr.dto.CommentResponseDTO;
import com.gderuki.taskr.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.Tasks.BASE + "/{taskId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management APIs for task comments/notes")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "Create a new comment for a task",
            description = "Creates a new comment/note for a specific task. The authenticated user will be set as the author."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "content": "This task needs to be completed by end of week",
                                              "taskId": 1,
                                              "authorId": 1,
                                              "authorUsername": "john.doe",
                                              "createdAt": "2026-01-04T10:15:30",
                                              "updatedAt": "2026-01-04T10:15:30"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment(
            @Parameter(description = "Task ID", example = "1", required = true)
            @PathVariable Long taskId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Comment details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": "This task needs to be completed by end of week"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CommentRequestDTO commentRequestDTO) {
        CommentResponseDTO createdComment = commentService.createComment(taskId, commentRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @Operation(
            summary = "Get all comments for a task",
            description = "Retrieves all comments for a specific task with pagination support."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comments retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "content": "This task needs to be completed by end of week",
                                                  "taskId": 1,
                                                  "authorId": 1,
                                                  "authorUsername": "john.doe",
                                                  "createdAt": "2026-01-04T10:15:30",
                                                  "updatedAt": "2026-01-04T10:15:30"
                                                },
                                                {
                                                  "id": 2,
                                                  "content": "Working on this now",
                                                  "taskId": 1,
                                                  "authorId": 2,
                                                  "authorUsername": "jane.smith",
                                                  "createdAt": "2026-01-04T11:00:00",
                                                  "updatedAt": "2026-01-04T11:00:00"
                                                }
                                              ],
                                              "pageable": {
                                                "pageNumber": 0,
                                                "pageSize": 10
                                              },
                                              "totalElements": 2,
                                              "totalPages": 1,
                                              "last": true
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<Page<CommentResponseDTO>> getCommentsByTaskId(
            @Parameter(description = "Task ID", example = "1", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponseDTO> comments = commentService.getCommentsByTaskId(taskId, pageable);
        return ResponseEntity.ok(comments);
    }

    @Operation(
            summary = "Get comment by ID",
            description = "Retrieves a specific comment for a task by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "content": "This task needs to be completed by end of week",
                                              "taskId": 1,
                                              "authorId": 1,
                                              "authorUsername": "john.doe",
                                              "createdAt": "2026-01-04T10:15:30",
                                              "updatedAt": "2026-01-04T10:15:30"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment or task not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> getCommentById(
            @Parameter(description = "Task ID", example = "1", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Comment ID", example = "1", required = true)
            @PathVariable Long commentId) {
        CommentResponseDTO comment = commentService.getCommentById(taskId, commentId);
        return ResponseEntity.ok(comment);
    }

    @Operation(
            summary = "Update a comment",
            description = "Updates an existing comment. Only the comment author can update it."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "content": "Updated: This task needs to be completed by end of week",
                                              "taskId": 1,
                                              "authorId": 1,
                                              "authorUsername": "john.doe",
                                              "createdAt": "2026-01-04T10:15:30",
                                              "updatedAt": "2026-01-04T11:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only the comment author can update this comment",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment or task not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @Parameter(description = "Task ID", example = "1", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Comment ID", example = "1", required = true)
            @PathVariable Long commentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated comment details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": "Updated: This task needs to be completed by end of week"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CommentRequestDTO commentRequestDTO) {

        CommentResponseDTO updatedComment = commentService.updateComment(taskId, commentId, commentRequestDTO);
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(
            summary = "Delete a comment",
            description = "Soft deletes a comment. Only the comment author can delete it. The comment is marked as deleted but not removed from the database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Comment deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Only the comment author can delete this comment",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment or task not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Task ID", example = "1", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "Comment ID", example = "1", required = true)
            @PathVariable Long commentId) {
        commentService.deleteComment(taskId, commentId);
        return ResponseEntity.noContent().build();
    }
}
