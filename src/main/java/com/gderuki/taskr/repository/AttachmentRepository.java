package com.gderuki.taskr.repository;

import com.gderuki.taskr.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /**
     * Find all non-deleted attachments for a specific task
     */
    @Query("SELECT a FROM Attachment a WHERE a.task.id = :taskId AND a.deletedAt IS NULL")
    List<Attachment> findByTaskIdAndNotDeleted(@Param("taskId") Long taskId);

    /**
     * Find a non-deleted attachment by ID
     */
    @Query("SELECT a FROM Attachment a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Attachment> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Find a non-deleted attachment by ID and task ID
     */
    @Query("SELECT a FROM Attachment a WHERE a.id = :id AND a.task.id = :taskId AND a.deletedAt IS NULL")
    Optional<Attachment> findByIdAndTaskIdAndNotDeleted(@Param("id") Long id, @Param("taskId") Long taskId);

    /**
     * Check if a non-deleted attachment exists by ID
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attachment a WHERE a.id = :id AND a.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Count non-deleted attachments for a specific task
     */
    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.task.id = :taskId AND a.deletedAt IS NULL")
    long countByTaskIdAndNotDeleted(@Param("taskId") Long taskId);
}
