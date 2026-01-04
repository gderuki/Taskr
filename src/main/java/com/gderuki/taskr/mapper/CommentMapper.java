package com.gderuki.taskr.mapper;

import com.gderuki.taskr.dto.CommentRequestDTO;
import com.gderuki.taskr.dto.CommentResponseDTO;
import com.gderuki.taskr.entity.Comment;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    /**
     * Convert CommentRequestDTO to Comment entity
     */
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true)
    Comment toEntity(CommentRequestDTO commentRequestDTO);

    /**
     * Convert Comment entity to CommentResponseDTO
     */
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    CommentResponseDTO toDto(Comment comment);

    /**
     * Update existing Comment entity from CommentRequestDTO
     * Only updates content field
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDto(CommentRequestDTO commentRequestDTO, @MappingTarget Comment comment);
}
