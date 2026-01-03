package com.gderuki.taskr.controller;

import com.gderuki.taskr.config.ApiConstants;
import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.dto.TaskResponseDTO;
import com.gderuki.taskr.service.TaskService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.Tasks.BASE)
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management APIs for creating, reading, updating, and deleting tasks")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Create a new task",
            description = "Creates a new task with title, description, and status. Requires authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "title": "Complete project documentation",
                                              "description": "Write comprehensive API documentation using SpringDoc OpenAPI",
                                              "status": "TODO",
                                              "createdAt": "2026-01-03T10:15:30",
                                              "updatedAt": "2026-01-03T10:15:30"
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
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Task details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "title": "Complete project documentation",
                                              "description": "Write comprehensive API documentation using SpringDoc OpenAPI",
                                              "status": "TODO"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        TaskResponseDTO createdTask = taskService.createTask(taskRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @Operation(
            summary = "Get all tasks",
            description = "Retrieves all tasks with pagination and sorting support. Requires authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "title": "Complete project documentation",
                                                  "description": "Write comprehensive API documentation using SpringDoc OpenAPI",
                                                  "status": "TODO",
                                                  "createdAt": "2026-01-03T10:15:30",
                                                  "updatedAt": "2026-01-03T10:15:30"
                                                },
                                                {
                                                  "id": 2,
                                                  "title": "Fix authentication bug",
                                                  "description": "Resolve JWT token refresh issue",
                                                  "status": "IN_PROGRESS",
                                                  "createdAt": "2026-01-03T09:00:00",
                                                  "updatedAt": "2026-01-03T09:30:00"
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
                    responseCode = "401",
                    description = "Unauthorized - JWT token missing or invalid",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> getAllTasks(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TaskResponseDTO> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "Get task by ID",
            description = "Retrieves a specific task by its ID. Requires authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "title": "Complete project documentation",
                                              "description": "Write comprehensive API documentation using SpringDoc OpenAPI",
                                              "status": "TODO",
                                              "createdAt": "2026-01-03T10:15:30",
                                              "updatedAt": "2026-01-03T10:15:30"
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
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @Parameter(description = "Task ID", example = "1", required = true)
            @PathVariable Long id) {
        TaskResponseDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @Operation(
            summary = "Update a task",
            description = "Updates an existing task by its ID. Requires authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "title": "Complete project documentation",
                                              "description": "Write comprehensive API documentation using SpringDoc OpenAPI",
                                              "status": "IN_PROGRESS",
                                              "createdAt": "2026-01-03T10:15:30",
                                              "updatedAt": "2026-01-03T11:20:00"
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
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @Parameter(description = "Task ID", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated task details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "title": "Complete project documentation",
                                              "description": "Write comprehensive API documentation using SpringDoc OpenAPI",
                                              "status": "IN_PROGRESS"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody TaskRequestDTO taskRequestDTO) {

        TaskResponseDTO updatedTask = taskService.updateTask(id, taskRequestDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(
            summary = "Delete a task",
            description = "Soft deletes a task by its ID. The task is marked as deleted but not removed from the database. Requires authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Task deleted successfully"
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID", example = "1", required = true)
            @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
