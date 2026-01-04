package com.gderuki.taskr.mapper;

import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.dto.TaskResponseDTO;
import com.gderuki.taskr.entity.Task;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    /**
     * Convert TaskRequestDTO to Task entity
     */
    @Mapping(target = "assignee", ignore = true)
    Task toEntity(TaskRequestDTO taskRequestDTO);

    /**
     * Convert Task entity to TaskResponseDTO
     */
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeUsername", source = "assignee.username")
    TaskResponseDTO toDto(Task task);

    /**
     * Update existing Task entity from TaskRequestDTO
     * Ignores id, createdAt, updatedAt, deletedAt, assignee fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    void updateEntityFromDto(TaskRequestDTO taskRequestDTO, @MappingTarget Task task);
}
