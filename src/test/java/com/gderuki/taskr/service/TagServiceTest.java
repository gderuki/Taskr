package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.TagDTO;
import com.gderuki.taskr.dto.TagRequestDTO;
import com.gderuki.taskr.entity.Tag;
import com.gderuki.taskr.exception.DuplicateTagException;
import com.gderuki.taskr.exception.TagNotFoundException;
import com.gderuki.taskr.mapper.TagMapper;
import com.gderuki.taskr.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService Tests")
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    private Tag tag;
    private TagDTO tagDTO;
    private TagRequestDTO tagRequestDTO;

    @BeforeEach
    void setUp() {
        tag = Tag.builder()
                .id(1L)
                .name("Bug")
                .color("#FF0000")
                .createdAt(LocalDateTime.now())
                .build();

        tagDTO = TagDTO.builder()
                .id(1L)
                .name("Bug")
                .color("#FF0000")
                .createdAt(LocalDateTime.now())
                .build();

        tagRequestDTO = TagRequestDTO.builder()
                .name("Bug")
                .color("#FF0000")
                .build();
    }

    @Test
    @DisplayName("Should create tag successfully")
    void shouldCreateTagSuccessfully() {
        // Given
        when(tagRepository.existsByNameIgnoreCase(tagRequestDTO.getName())).thenReturn(false);
        when(tagMapper.toEntity(tagRequestDTO)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDto(tag)).thenReturn(tagDTO);

        // When
        TagDTO result = tagService.createTag(tagRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Bug");
        assertThat(result.getColor()).isEqualTo("#FF0000");
        verify(tagRepository).existsByNameIgnoreCase(tagRequestDTO.getName());
        verify(tagRepository).save(tag);
    }

    @Test
    @DisplayName("Should throw DuplicateTagException when creating tag with existing name")
    void shouldThrowDuplicateTagExceptionWhenCreatingWithExistingName() {
        // Given
        when(tagRepository.existsByNameIgnoreCase(tagRequestDTO.getName())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> tagService.createTag(tagRequestDTO))
                .isInstanceOf(DuplicateTagException.class)
                .hasMessageContaining("Bug");
        verify(tagRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get all tags with pagination")
    void shouldGetAllTagsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Tag> tags = Collections.singletonList(tag);
        Page<Tag> tagPage = new PageImpl<>(tags, pageable, tags.size());
        when(tagRepository.findAll(pageable)).thenReturn(tagPage);
        when(tagMapper.toDto(tag)).thenReturn(tagDTO);

        // When
        Page<TagDTO> result = tagService.getAllTags(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Bug");
        verify(tagRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get all tags as list")
    void shouldGetAllTagsAsList() {
        // Given
        List<Tag> tags = Collections.singletonList(tag);
        when(tagRepository.findAll()).thenReturn(tags);
        when(tagMapper.toDto(tag)).thenReturn(tagDTO);

        // When
        List<TagDTO> result = tagService.getAllTags();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Bug");
        verify(tagRepository).findAll();
    }

    @Test
    @DisplayName("Should get tag by ID")
    void shouldGetTagById() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagMapper.toDto(tag)).thenReturn(tagDTO);

        // When
        TagDTO result = tagService.getTagById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Bug");
        verify(tagRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw TagNotFoundException when tag not found")
    void shouldThrowTagNotFoundExceptionWhenTagNotFound() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tagService.getTagById(1L))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessageContaining("1");
        verify(tagRepository).findById(1L);
    }

    @Test
    @DisplayName("Should update tag successfully")
    void shouldUpdateTagSuccessfully() {
        // Given
        TagRequestDTO updateRequest = TagRequestDTO.builder()
                .name("Critical Bug")
                .color("#FF0000")
                .build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findByNameIgnoreCase("Critical Bug")).thenReturn(Optional.empty());
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDto(tag)).thenReturn(tagDTO);

        // When
        TagDTO result = tagService.updateTag(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(tagRepository).findById(1L);
        verify(tagMapper).updateEntityFromDto(updateRequest, tag);
        verify(tagRepository).save(tag);
    }

    @Test
    @DisplayName("Should throw DuplicateTagException when updating to existing name")
    void shouldThrowDuplicateTagExceptionWhenUpdatingToExistingName() {
        // Given
        Tag existingTag = Tag.builder()
                .id(2L)
                .name("Feature")
                .build();

        TagRequestDTO updateRequest = TagRequestDTO.builder()
                .name("Feature")
                .build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findByNameIgnoreCase("Feature")).thenReturn(Optional.of(existingTag));

        // When & Then
        assertThatThrownBy(() -> tagService.updateTag(1L, updateRequest))
                .isInstanceOf(DuplicateTagException.class)
                .hasMessageContaining("Feature");
        verify(tagRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete tag successfully")
    void shouldDeleteTagSuccessfully() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        // When
        tagService.deleteTag(1L);

        // Then
        verify(tagRepository).findById(1L);
        verify(tagRepository).delete(tag);
    }

    @Test
    @DisplayName("Should throw TagNotFoundException when deleting non-existent tag")
    void shouldThrowTagNotFoundExceptionWhenDeletingNonExistentTag() {
        // Given
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tagService.deleteTag(1L))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessageContaining("1");
        verify(tagRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get tags by IDs")
    void shouldGetTagsByIds() {
        // Given
        Set<Long> tagIds = Set.of(1L, 2L);
        Set<Tag> tags = Set.of(tag);
        when(tagRepository.findByIdIn(tagIds)).thenReturn(tags);

        // When
        Set<Tag> result = tagService.getTagsByIds(tagIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(tagRepository).findByIdIn(tagIds);
    }

    @Test
    @DisplayName("Should get or create tags by names")
    void shouldGetOrCreateTagsByNames() {
        // Given
        Set<String> tagNamesInput = Set.of("Bug", "NewTag");
        Set<String> normalizedTagNames = Set.of("bug", "newtag");

        Set<Tag> existingTags = new HashSet<>(Set.of(tag));

        Tag newTag = Tag.builder()
                .name("NewTag")
                .build();

        when(tagRepository.findByNameInIgnoreCaseLower(normalizedTagNames)).thenReturn(existingTags);
        when(tagRepository.save(any(Tag.class))).thenReturn(newTag);

        // When
        Set<Tag> result = tagService.getOrCreateTagsByNames(tagNamesInput);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(1);
        verify(tagRepository).findByNameInIgnoreCaseLower(normalizedTagNames);
        verify(tagRepository, atLeastOnce()).save(any(Tag.class));
    }
}
