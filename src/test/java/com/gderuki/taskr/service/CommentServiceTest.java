package com.gderuki.taskr.service;

import com.gderuki.taskr.dto.CommentRequestDTO;
import com.gderuki.taskr.dto.CommentResponseDTO;
import com.gderuki.taskr.entity.Comment;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.TaskPriority;
import com.gderuki.taskr.entity.TaskStatus;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.exception.CommentNotFoundException;
import com.gderuki.taskr.exception.TaskNotFoundException;
import com.gderuki.taskr.mapper.CommentMapper;
import com.gderuki.taskr.repository.CommentRepository;
import com.gderuki.taskr.repository.TaskRepository;
import com.gderuki.taskr.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private CommentService commentService;

    private Task task;
    private User user;
    private Comment comment;
    private CommentRequestDTO commentRequestDTO;
    private CommentResponseDTO commentResponseDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        comment = Comment.builder()
                .id(1L)
                .content("Test comment")
                .task(task)
                .author(user)
                .build();

        commentRequestDTO = CommentRequestDTO.builder()
                .content("Test comment")
                .build();

        commentResponseDTO = CommentResponseDTO.builder()
                .id(1L)
                .content("Test comment")
                .taskId(1L)
                .authorId(1L)
                .authorUsername("testuser")
                .build();
    }

    private void mockSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void createComment_ShouldReturnCommentResponseDTO() {
        mockSecurityContext();
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(commentMapper.toEntity(any(CommentRequestDTO.class))).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDTO);

        CommentResponseDTO result = commentService.createComment(1L, commentRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(commentRequestDTO.getContent());
        verify(taskRepository).findByIdAndNotDeleted(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_WhenTaskDoesNotExist_ShouldThrowException() {
        when(taskRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(1L, commentRequestDTO))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void getCommentsByTaskId_ShouldReturnPageOfCommentResponseDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = new PageImpl<>(List.of(comment));

        when(taskRepository.existsByIdAndNotDeleted(1L)).thenReturn(true);
        when(commentRepository.findAllByTaskId(1L, pageable)).thenReturn(commentPage);
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDTO);

        Page<CommentResponseDTO> result = commentService.getCommentsByTaskId(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).existsByIdAndNotDeleted(1L);
        verify(commentRepository).findAllByTaskId(1L, pageable);
    }

    @Test
    void getCommentsByTaskId_WhenTaskDoesNotExist_ShouldThrowException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(taskRepository.existsByIdAndNotDeleted(1L)).thenReturn(false);

        assertThatThrownBy(() -> commentService.getCommentsByTaskId(1L, pageable))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void getCommentById_WhenCommentExists_ShouldReturnCommentResponseDTO() {
        when(commentRepository.findByIdAndTaskIdAndNotDeleted(1L, 1L)).thenReturn(Optional.of(comment));
        when(commentMapper.toDto(comment)).thenReturn(commentResponseDTO);

        CommentResponseDTO result = commentService.getCommentById(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(commentRepository).findByIdAndTaskIdAndNotDeleted(1L, 1L);
    }

    @Test
    void getCommentById_WhenCommentDoesNotExist_ShouldThrowException() {
        when(commentRepository.findByIdAndTaskIdAndNotDeleted(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getCommentById(1L, 1L))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void updateComment_WhenCommentExistsAndUserIsAuthor_ShouldReturnUpdatedCommentResponseDTO() {
        mockSecurityContext();
        when(commentRepository.findByIdAndTaskIdAndNotDeleted(1L, 1L)).thenReturn(Optional.of(comment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        doNothing().when(commentMapper).updateEntityFromDto(any(CommentRequestDTO.class), any(Comment.class));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDto(any(Comment.class))).thenReturn(commentResponseDTO);

        CommentResponseDTO result = commentService.updateComment(1L, 1L, commentRequestDTO);

        assertThat(result).isNotNull();
        verify(commentRepository).save(comment);
    }

    @Test
    void updateComment_WhenCommentDoesNotExist_ShouldThrowException() {
        when(commentRepository.findByIdAndTaskIdAndNotDeleted(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.updateComment(1L, 1L, commentRequestDTO))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void updateComment_WhenUserIsNotAuthor_ShouldThrowException() {
        mockSecurityContext();
        User differentUser = User.builder()
                .id(2L)
                .username("differentuser")
                .email("different@example.com")
                .password("password")
                .build();
        comment.setAuthor(differentUser);

        when(commentRepository.findByIdAndTaskIdAndNotDeleted(1L, 1L)).thenReturn(Optional.of(comment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> commentService.updateComment(1L, 1L, commentRequestDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only the comment author can update this comment");
    }

    @Test
    void deleteComment_WhenCommentExistsAndUserIsAuthor_ShouldSoftDeleteComment() {
        mockSecurityContext();
        when(commentRepository.findByIdAndTaskIdAndNotDeleted(1L, 1L)).thenReturn(Optional.of(comment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        commentService.deleteComment(1L, 1L);

        assertThat(comment.getDeletedAt()).isNotNull();
        verify(commentRepository).save(comment);
    }

    @Test
    void deleteComment_WhenCommentDoesNotExist_ShouldThrowException() {
        when(commentRepository.findByIdAndTaskIdAndNotDeleted(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(1L, 1L))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void deleteComment_WhenUserIsNotAuthor_ShouldThrowException() {
        mockSecurityContext();
        User differentUser = User.builder()
                .id(2L)
                .username("differentuser")
                .email("different@example.com")
                .password("password")
                .build();
        comment.setAuthor(differentUser);

        when(commentRepository.findByIdAndTaskIdAndNotDeleted(1L, 1L)).thenReturn(Optional.of(comment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> commentService.deleteComment(1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only the comment author can delete this comment");
    }
}
