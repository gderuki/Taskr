package com.gderuki.taskr.repository;

import com.gderuki.taskr.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Find a tag by its name (case-insensitive)
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) = LOWER(:name)")
    Optional<Tag> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Check if a tag exists by name (case-insensitive)
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Tag t WHERE LOWER(t.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Find tags by IDs
     */
    @Query("SELECT t FROM Tag t WHERE t.id IN :ids")
    Set<Tag> findByIdIn(@Param("ids") Set<Long> ids);

    /**
     * Find tags by names (case-insensitive)
     * Note: The caller should already lowercase the names parameter
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) IN :names")
    Set<Tag> findByNameInIgnoreCaseLower(@Param("names") Set<String> names);
}
