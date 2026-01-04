package com.gderuki.taskr.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Custom annotation for testing with a mock CustomUserDetails.
 * This properly sets up the security context with our CustomUserDetails
 * including the user ID, which is required for audit logging.
 * <p>
 * Usage:
 * @WithMockCustomUser(id = 1L, username = "testuser")
 * void someTest() { ... }
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    /**
     * The user ID to use in the security context
     */
    long id() default 1L;

    /**
     * The username to use in the security context
     */
    String username() default "testuser";

    /**
     * The password to use (not validated in tests)
     */
    String password() default "password";

    /**
     * Whether the user is enabled
     */
    boolean enabled() default true;
}
