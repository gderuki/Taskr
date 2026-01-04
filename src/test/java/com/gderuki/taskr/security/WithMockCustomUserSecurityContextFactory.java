package com.gderuki.taskr.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;

/**
 * Factory for creating a SecurityContext with CustomUserDetails for testing.
 * This is used by the @WithMockCustomUser annotation to properly set up
 * the security context with our custom user details including the user ID.
 */
public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        CustomUserDetails principal = new CustomUserDetails(
                annotation.id(),
                annotation.username(),
                annotation.password(),
                annotation.enabled(),
                new ArrayList<>()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                annotation.password(),
                principal.getAuthorities()
        );

        context.setAuthentication(auth);
        return context;
    }
}
