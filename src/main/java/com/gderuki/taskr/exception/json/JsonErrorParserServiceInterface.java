package com.gderuki.taskr.exception.json;

/**
 * Service interface for parsing JSON deserialization errors
 */
public interface JsonErrorParserServiceInterface {

    /**
     * Parse the error message using the highest priority matching parser
     *
     * @param errorMessage The raw error message from Jackson
     * @return User-friendly error message
     */
    String parseError(String errorMessage);
}
