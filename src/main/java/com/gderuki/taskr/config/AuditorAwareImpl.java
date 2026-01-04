package com.gderuki.taskr.config;

import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementation of AuditorAware to provide the current auditor (user ID) for JPA auditing.
 * This class retrieves the currently authenticated user from the Spring Security context.
 * <p>
 * Note: This expects CustomUserDetails in the security context. For tests, use
 * @WithMockCustomUser annotation instead of @WithMockUser to properly set up the context.
 */
@Slf4j
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public @NonNull Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            log.debug("No authenticated user found for auditing");
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        Long userId = null;
        String username = null;

        if (principal instanceof CustomUserDetails userDetails) {
            userId = userDetails.getId();
            username = userDetails.getUsername();
        } else if (principal instanceof User user) {
            userId = user.getId();
            username = user.getUsername();
        }

        if (userId != null) {
            log.debug("Auditing: User ID {} ({})", userId, username);
            return Optional.of(userId);
        }

        log.warn("Unknown principal type for auditing: {}", principal.getClass().getName());
        return Optional.empty();
    }
}
