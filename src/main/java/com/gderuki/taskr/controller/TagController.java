package com.gderuki.taskr.controller;

import com.gderuki.taskr.dto.TagDTO;
import com.gderuki.taskr.dto.TagRequestDTO;
import com.gderuki.taskr.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tag Management", description = "APIs for managing task tags/categories")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Create a new tag", description = "Creates a new tag with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tag created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate tag name"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagRequestDTO tagRequestDTO) {
        log.info("REST request to create tag: {}", tagRequestDTO.getName());
        TagDTO createdTag = tagService.createTag(tagRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    @Operation(summary = "Get all tags with pagination", description = "Retrieves all tags with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<Page<TagDTO>> getAllTags(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by (name, createdAt)", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

        log.info("REST request to get all tags with pagination");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TagDTO> tags = tagService.getAllTags(pageable);
        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "Get all tags without pagination", description = "Retrieves all tags as a list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/all")
    public ResponseEntity<List<TagDTO>> getAllTagsList() {
        log.info("REST request to get all tags as list");
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "Get tag by ID", description = "Retrieves a specific tag by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag found"),
            @ApiResponse(responseCode = "404", description = "Tag not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        log.info("REST request to get tag by id: {}", id);
        TagDTO tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }

    @Operation(summary = "Update tag", description = "Updates an existing tag with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate tag name"),
            @ApiResponse(responseCode = "404", description = "Tag not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequestDTO tagRequestDTO) {
        log.info("REST request to update tag with id: {}", id);
        TagDTO updatedTag = tagService.updateTag(id, tagRequestDTO);
        return ResponseEntity.ok(updatedTag);
    }

    @Operation(summary = "Delete tag", description = "Deletes a tag by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.info("REST request to delete tag with id: {}", id);
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
