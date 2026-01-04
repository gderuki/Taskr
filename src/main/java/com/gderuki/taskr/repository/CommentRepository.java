package com.gderuki.taskr.repository;

import com.gderuki.taskr.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all non-deleted comments for a specific task with pagination
     */
    @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findAllByTaskId(@Param("taskId") Long taskId, Pageable pageable);

    /**
     * Find a non-deleted comment by ID
     */
    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Comment> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Find a non-deleted comment by ID and task ID
     */
    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.task.id = :taskId AND c.deletedAt IS NULL")
    Optional<Comment> findByIdAndTaskIdAndNotDeleted(@Param("id") Long id, @Param("taskId") Long taskId);

    /**
     * Count non-deleted comments for a specific task
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.task.id = :taskId AND c.deletedAt IS NULL")
    long countByTaskId(@Param("taskId") Long taskId);
}
