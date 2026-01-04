package com.gderuki.taskr.specification;

import com.gderuki.taskr.dto.TaskSearchCriteria;
import com.gderuki.taskr.entity.Tag;
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

            boolean hasTagFilter = false;

            if (criteria.getTagIds() != null && !criteria.getTagIds().isEmpty()) {
                if (query != null) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<Task> subqueryTask = subquery.from(Task.class);
                    Join<Task, Tag> subqueryTagJoin = subqueryTask.join("tags");

                    subquery.select(subqueryTask.get("id"))
                            .where(
                                    criteriaBuilder.equal(subqueryTask.get("id"), root.get("id")),
                                    subqueryTagJoin.get("id").in(criteria.getTagIds())
                            )
                            .groupBy(subqueryTask.get("id"))
                            .having(criteriaBuilder.equal(
                                    criteriaBuilder.count(subqueryTagJoin.get("id")),
                                    (long) criteria.getTagIds().size()
                            ));

                    predicates.add(root.get("id").in(subquery));
                    hasTagFilter = true;
                }
            }

            if (!hasTagFilter && criteria.getAnyTagIds() != null && !criteria.getAnyTagIds().isEmpty()) {
                Join<Task, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(tagJoin.get("id").in(criteria.getAnyTagIds()));

                if (query != null) {
                    query.distinct(true);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
