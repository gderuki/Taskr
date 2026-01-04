package com.gderuki.taskr.config;

import com.gderuki.taskr.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for AuditorAwareImpl.
 * Tests that our auditing configuration correctly extracts user IDs from the security context.
 */
class AuditorAwareImplTest {

    private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_WithCustomUserDetails_ShouldReturnUserId() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(42L, "testuser", "password", true, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(userDetails, "password", userDetails.getAuthorities())
        );

        // When
        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        // Then
        assertThat(auditor).isPresent();
        assertThat(auditor.get()).isEqualTo(42L);
    }

    @Test
    void getCurrentAuditor_WithNoAuthentication_ShouldReturnEmpty() {
        // Given
        SecurityContextHolder.clearContext();

        // When
        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        // Then
        assertThat(auditor).isEmpty();
    }

    @Test
    void getCurrentAuditor_WithAnonymousUser_ShouldReturnEmpty() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("anonymousUser", null, new ArrayList<>())
        );

        // When
        Optional<Long> auditor = auditorAware.getCurrentAuditor();

        // Then
        assertThat(auditor).isEmpty();
    }
}
