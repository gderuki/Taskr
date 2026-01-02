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
     * Authentication API constants.
     */
    public static final class Auth {
        public static final String BASE = API_BASE + "/auth";

        private Auth() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }

    /**
     * Tasks API constants.
     */
    public static final class Tasks {
        public static final String BASE = API_BASE + "/tasks";

        private Tasks() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }

    /**
     * Security path patterns for configuration.
     */
    public static final class Patterns {
        public static final String API_ALL = "/api/**";
        public static final String AUTH_ALL = "/api/*/auth/**";
        public static final String ACTUATOR_ALL = "/actuator/**";

        private Patterns() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }

    private ApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
