package com.gderuki.taskr.config;

/**
 * Constants for API routing and path patterns.
 */
public final class ApiConstants {

    /**
     * API version.
     */
    public static final String API_VERSION = "v1";

    /**
     * Base API path with version.
     */
    public static final String API_BASE = "/api/" + API_VERSION;

    /**
     * Authentication path.
     */
    public static final String AUTH_PATH = API_BASE + "/auth";

    /**
     * Tasks path.
     */
    public static final String TASKS_PATH = API_BASE + "/tasks";

    /**
     * Base path pattern for all API endpoints.
     */
    public static final String API_PATH_PATTERN = "/api/**";

    /**
     * Path pattern for authentication endpoints.
     */
    public static final String AUTH_PATH_PATTERN = "/api/*/auth/**";

    /**
     * Path pattern for actuator endpoints.
     */
    public static final String ACTUATOR_PATH_PATTERN = "/actuator/**";

    private ApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
