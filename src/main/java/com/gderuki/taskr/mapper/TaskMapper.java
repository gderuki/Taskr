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
    Task toEntity(TaskRequestDTO taskRequestDTO);

    /**
     * Convert Task entity to TaskResponseDTO
     */
    TaskResponseDTO toDto(Task task);

    /**
     * Update existing Task entity from TaskRequestDTO
     * Ignores id, createdAt, updatedAt, deletedAt fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDto(TaskRequestDTO taskRequestDTO, @MappingTarget Task task);
}
