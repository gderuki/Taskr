package com.gderuki.taskr.repository;

import com.gderuki.taskr.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

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
}
