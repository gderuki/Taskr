package com.gderuki.taskr.mapper;

import com.gderuki.taskr.dto.TagDTO;
import com.gderuki.taskr.dto.TagRequestDTO;
import com.gderuki.taskr.entity.Tag;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {

    /**
     * Convert TagRequestDTO to Tag entity
     */
    Tag toEntity(TagRequestDTO tagRequestDTO);

    /**
     * Convert Tag entity to TagDTO
     */
    TagDTO toDto(Tag tag);

    /**
     * Update an existing Tag entity from TagRequestDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void updateEntityFromDto(TagRequestDTO tagRequestDTO, @MappingTarget Tag tag);
}
