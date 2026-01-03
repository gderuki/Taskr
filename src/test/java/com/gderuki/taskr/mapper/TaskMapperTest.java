package com.gderuki.taskr.mapper;

import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.dto.TaskResponseDTO;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

    private final TaskMapper mapper = Mappers.getMapper(TaskMapper.class);

    @Test
    void toEntity_ShouldMapCorrectly() {
        TaskRequestDTO dto = TaskRequestDTO.builder()
                .title("Title")
                .description("Desc")
                .status(TaskStatus.TODO)
                .build();

        Task entity = mapper.toEntity(dto);

        assertThat(entity.getTitle()).isEqualTo(dto.getTitle());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.getStatus()).isEqualTo(dto.getStatus());
    }

    @Test
    void toDto_ShouldMapCorrectly() {
        Task entity = Task.builder()
                .id(1L)
                .title("Title")
                .description("Desc")
                .status(TaskStatus.TODO)
                .build();

        TaskResponseDTO dto = mapper.toDto(entity);

        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getTitle()).isEqualTo(entity.getTitle());
        assertThat(dto.getDescription()).isEqualTo(entity.getDescription());
        assertThat(dto.getStatus()).isEqualTo(entity.getStatus());
    }

    @Test
    void updateEntityFromDto_ShouldUpdateFields() {
        Task entity = Task.builder()
                .id(1L)
                .title("Old Title")
                .description("Old Desc")
                .status(TaskStatus.TODO)
                .build();

        TaskRequestDTO dto = TaskRequestDTO.builder()
                .title("New Title")
                .description("New Desc")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        mapper.updateEntityFromDto(dto, entity);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getTitle()).isEqualTo("New Title");
        assertThat(entity.getDescription()).isEqualTo("New Desc");
        assertThat(entity.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }
}
