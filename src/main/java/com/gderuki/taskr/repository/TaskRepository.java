package com.gderuki.taskr.repository;

import com.gderuki.taskr.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Find all non-deleted tasks with pagination and sorting
     */
    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NULL")
    Page<Task> findAllActive(Pageable pageable);

    /**
     * Find a non-deleted task by ID
     */
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Task> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Check if a non-deleted task exists by ID
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Task t WHERE t.id = :id AND t.deletedAt IS NULL")
    boolean existsByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Find all non-deleted tasks with due dates between start and end time
     */
    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NULL AND t.dueDate BETWEEN :startTime AND :endTime ORDER BY t.dueDate ASC")
    List<Task> findTasksWithDueDateBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * Find all overdue non-deleted tasks (due date is in the past and status is not DONE)
     */
    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NULL AND t.dueDate < :now AND t.status <> 'DONE' ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);
}
