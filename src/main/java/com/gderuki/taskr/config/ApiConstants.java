package com.gderuki.taskr.config;

/**
 * Constants for API routing and path patterns.
 */
public final class ApiConstants {

    /**
     * Base path pattern for all API endpoints.
     */
    public static final String API_PATH_PATTERN = "/api/**";

    private ApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
