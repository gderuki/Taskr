package com.gderuki.taskr.mapper;

import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.dto.TaskResponseDTO;
import com.gderuki.taskr.entity.Task;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {TagMapper.class, UserMapper.class})
public interface TaskMapper {

    /**
     * Convert TaskRequestDTO to Task entity
     */
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Task toEntity(TaskRequestDTO taskRequestDTO);

    /**
     * Convert Task entity to TaskResponseDTO
     */
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeUsername", source = "assignee.username")
    @Mapping(target = "createdById", source = "createdBy")
    @Mapping(target = "createdByUsername", source = "createdBy", qualifiedByName = "userIdToUsername")
    @Mapping(target = "modifiedById", source = "modifiedBy")
    @Mapping(target = "modifiedByUsername", source = "modifiedBy", qualifiedByName = "userIdToUsername")
    @Mapping(target = "deletedById", source = "deletedBy")
    @Mapping(target = "deletedByUsername", source = "deletedBy", qualifiedByName = "userIdToUsername")
    TaskResponseDTO toDto(Task task);

    /**
     * Update existing Task entity from TaskRequestDTO
     * Ignores id, createdAt, updatedAt, deletedAt, assignee, tags, and audit fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(TaskRequestDTO taskRequestDTO, @MappingTarget Task task);
}
