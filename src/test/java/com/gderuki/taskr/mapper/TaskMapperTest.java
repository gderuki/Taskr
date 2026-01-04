package com.gderuki.taskr.mapper;

import com.gderuki.taskr.dto.TaskRequestDTO;
import com.gderuki.taskr.dto.TaskResponseDTO;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskMapperTest {

    @Autowired
    private TaskMapper mapper;

    @Autowired
    private UserRepository userRepository;

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
    void toDto_WithAuditFields_ShouldMapUsernames() {
        userRepository.deleteAll();
        User creator = userRepository.save(User.builder()
                .username("creator")
                .email("creator@test.com")
                .password("password")
                .build());

        User modifier = userRepository.save(User.builder()
                .username("modifier")
                .email("modifier@test.com")
                .password("password")
                .build());

        Task entity = Task.builder()
                .id(1L)
                .title("Title")
                .status(TaskStatus.TODO)
                .createdBy(creator.getId())
                .modifiedBy(modifier.getId())
                .build();

        TaskResponseDTO dto = mapper.toDto(entity);

        assertThat(dto.getCreatedById()).isEqualTo(creator.getId());
        assertThat(dto.getCreatedByUsername()).isEqualTo("creator");
        assertThat(dto.getModifiedById()).isEqualTo(modifier.getId());
        assertThat(dto.getModifiedByUsername()).isEqualTo("modifier");
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
