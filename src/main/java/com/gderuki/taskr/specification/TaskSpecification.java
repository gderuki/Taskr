package com.gderuki.taskr.specification;

import com.gderuki.taskr.dto.TaskSearchCriteria;
import com.gderuki.taskr.entity.Task;
import com.gderuki.taskr.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    private TaskSpecification() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a specification based on search criteria
     *
     * @param criteria The search criteria
     * @return Specification for Task entity
     */
    public static Specification<Task> withCriteria(TaskSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
                String likePattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        likePattern
                );
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        likePattern
                );
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }

            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getPriority() != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), criteria.getPriority()));
            }

            if (criteria.getAssigneeId() != null) {
                Join<Task, User> assigneeJoin = root.join("assignee", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(assigneeJoin.get("id"), criteria.getAssigneeId()));
            }

            if (Boolean.TRUE.equals(criteria.getUnassignedOnly())) {
                predicates.add(criteriaBuilder.isNull(root.get("assignee")));
            }

            if (criteria.getDueDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dueDate"),
                        criteria.getDueDateFrom()
                ));
            }

            if (criteria.getDueDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dueDate"),
                        criteria.getDueDateTo()
                ));
            }

            if (criteria.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        criteria.getCreatedAfter()
                ));
            }

            if (criteria.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        criteria.getCreatedBefore()
                ));
            }

            if (Boolean.TRUE.equals(criteria.getOverdueOnly())) {
                LocalDateTime now = LocalDateTime.now();
                predicates.add(criteriaBuilder.and(
                        criteriaBuilder.lessThan(root.get("dueDate"), now),
                        criteriaBuilder.notEqual(root.get("status"), criteriaBuilder.literal("DONE"))
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
