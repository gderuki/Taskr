package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.CommentRequestDTO;
import com.gderuki.taskr.dto.CommentResponseDTO;
import com.gderuki.taskr.entity.Comment;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.exception.CommentNotFoundException;
import com.gderuki.taskr.exception.TaskNotFoundException;
import com.gderuki.taskr.mapper.CommentMapper;
import com.gderuki.taskr.repository.CommentRepository;
import com.gderuki.taskr.repository.TaskRepository;
import com.gderuki.taskr.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    @Timed(value = "taskr.comment.create", description = "Time taken to create a comment")
    public CommentResponseDTO createComment(Long taskId, CommentRequestDTO commentRequestDTO) {
        log.info("Creating new comment for task: {}", taskId);

        Task task = taskRepository.findByIdAndNotDeleted(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        User author = getCurrentUser();

        Comment comment = commentMapper.toEntity(commentRequestDTO);
        comment.setTask(task);
        comment.setAuthor(author);

        Comment savedComment = commentRepository.save(comment);

        log.info("Comment created successfully with id: {} for task: {}", savedComment.getId(), taskId);
        return commentMapper.toDto(savedComment);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.comment.getAllByTask", description = "Time taken to fetch all comments for a task")
    public Page<CommentResponseDTO> getCommentsByTaskId(Long taskId, Pageable pageable) {
        log.info("Fetching comments for task: {} with pagination: page={}, size={}",
                taskId, pageable.getPageNumber(), pageable.getPageSize());

        if (!taskRepository.existsByIdAndNotDeleted(taskId)) {
            throw new TaskNotFoundException(taskId);
        }

        Page<Comment> comments = commentRepository.findAllByTaskId(taskId, pageable);
        return comments.map(commentMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Timed(value = "taskr.comment.getById", description = "Time taken to fetch a comment by ID")
    public CommentResponseDTO getCommentById(Long taskId, Long commentId) {
        log.info("Fetching comment with id: {} for task: {}", commentId, taskId);

        Comment comment = commentRepository.findByIdAndTaskIdAndNotDeleted(commentId, taskId)
                .orElseThrow(() -> new CommentNotFoundException(commentId, taskId));

        return commentMapper.toDto(comment);
    }

    @Transactional
    @Timed(value = "taskr.comment.update", description = "Time taken to update a comment")
    public CommentResponseDTO updateComment(Long taskId, Long commentId, CommentRequestDTO commentRequestDTO) {
        log.info("Updating comment with id: {} for task: {}", commentId, taskId);

        Comment comment = commentRepository.findByIdAndTaskIdAndNotDeleted(commentId, taskId)
                .orElseThrow(() -> new CommentNotFoundException(commentId, taskId));

        User currentUser = getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Only the comment author can update this comment");
        }

        commentMapper.updateEntityFromDto(commentRequestDTO, comment);
        Comment updatedComment = commentRepository.save(comment);

        log.info("Comment updated successfully with id: {} for task: {}", commentId, taskId);
        return commentMapper.toDto(updatedComment);
    }

    @Transactional
    @Timed(value = "taskr.comment.delete", description = "Time taken to delete a comment")
    public void deleteComment(Long taskId, Long commentId) {
        log.info("Soft deleting comment with id: {} for task: {}", commentId, taskId);

        Comment comment = commentRepository.findByIdAndTaskIdAndNotDeleted(commentId, taskId)
                .orElseThrow(() -> new CommentNotFoundException(commentId, taskId));

        User currentUser = getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Only the comment author can delete this comment");
        }

        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);

        log.info("Comment soft deleted successfully with id: {} for task: {}", commentId, taskId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }
}
