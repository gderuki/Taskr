package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.TagDTO;
import com.gderuki.taskr.dto.TagRequestDTO;
import com.gderuki.taskr.entity.Tag;
import com.gderuki.taskr.exception.DuplicateTagException;
import com.gderuki.taskr.exception.TagNotFoundException;
import com.gderuki.taskr.mapper.TagMapper;
import com.gderuki.taskr.repository.TagRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Transactional
    @Timed(value = "taskr.tag.create", description = "Time taken to create a tag")
    public TagDTO createTag(TagRequestDTO tagRequestDTO) {
        log.info("Creating new tag with name: {}", tagRequestDTO.getName());

        if (tagRepository.existsByNameIgnoreCase(tagRequestDTO.getName())) {
            throw new DuplicateTagException(tagRequestDTO.getName());
        }

        Tag tag = tagMapper.toEntity(tagRequestDTO);
        Tag savedTag = tagRepository.save(tag);

        log.info("Tag created successfully with id: {}", savedTag.getId());
        return tagMapper.toDto(savedTag);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.tag.getAll", description = "Time taken to fetch all tags")
    public Page<TagDTO> getAllTags(Pageable pageable) {
        log.info("Fetching all tags with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Tag> tags = tagRepository.findAll(pageable);
        return tags.map(tagMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.tag.getAll", description = "Time taken to fetch all tags")
    public List<TagDTO> getAllTags() {
        log.info("Fetching all tags");

        List<Tag> tags = tagRepository.findAll();
        return tags.stream()
                .map(tagMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.tag.getById", description = "Time taken to fetch a tag by ID")
    public TagDTO getTagById(Long id) {
        log.info("Fetching tag with id: {}", id);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        return tagMapper.toDto(tag);
    }

    @Transactional
    @Timed(value = "taskr.tag.update", description = "Time taken to update a tag")
    public TagDTO updateTag(Long id, TagRequestDTO tagRequestDTO) {
        log.info("Updating tag with id: {}", id);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        tagRepository.findByNameIgnoreCase(tagRequestDTO.getName())
                .ifPresent(existingTag -> {
                    if (!existingTag.getId().equals(id)) {
                        throw new DuplicateTagException(tagRequestDTO.getName());
                    }
                });

        tagMapper.updateEntityFromDto(tagRequestDTO, tag);
        Tag updatedTag = tagRepository.save(tag);

        log.info("Tag updated successfully with id: {}", id);
        return tagMapper.toDto(updatedTag);
    }

    @Transactional
    @Timed(value = "taskr.tag.delete", description = "Time taken to delete a tag")
    public void deleteTag(Long id) {
        log.info("Deleting tag with id: {}", id);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        tagRepository.delete(tag);

        log.info("Tag deleted successfully with id: {}", id);
    }

    @Transactional(readOnly = true)
    public Set<Tag> getTagsByIds(Set<Long> tagIds) {
        log.debug("Fetching tags by IDs: {}", tagIds);
        return tagRepository.findByIdIn(tagIds);
    }

    @Transactional(readOnly = true)
    public Set<Tag> getOrCreateTagsByNames(Set<String> tagNames) {
        log.debug("Getting or creating tags by names: {}", tagNames);

        Set<String> lowerCaseNames = tagNames.stream()
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());

        Set<Tag> existingTags = tagRepository.findByNameInIgnoreCaseLower(lowerCaseNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(tag -> tag.getName().toLowerCase())
                .collect(java.util.stream.Collectors.toSet());

        for (String tagName : tagNames) {
            if (!existingTagNames.contains(tagName.toLowerCase())) {
                Tag newTag = Tag.builder()
                        .name(tagName)
                        .build();
                existingTags.add(tagRepository.save(newTag));
            }
        }

        return existingTags;
    }
}
