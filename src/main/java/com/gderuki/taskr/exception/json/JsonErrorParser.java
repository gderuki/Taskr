package com.gderuki.taskr.exception.json;

/**
 * Interface for parsing JSON deserialization errors
 */
public interface JsonErrorParser {

    /**
     * Check if this parser can handle the given error message
     */
    boolean canHandle(String errorMessage);

    /**
     * Parse the error message and return a user-friendly message
     */
    String parse(String errorMessage);

    /**
     * Get the priority of this parser (lower number = higher priority)
     * Used to determine which parser to use when multiple parsers can handle the error
     */
    default int getPriority() {
        return 100;
    }
}
