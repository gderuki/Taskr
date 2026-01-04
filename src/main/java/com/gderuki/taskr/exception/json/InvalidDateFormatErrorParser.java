package com.gderuki.taskr.exception.json;

import org.springframework.stereotype.Component;

/**
 * Parser for invalid date/time format errors
 */
@Component
public class InvalidDateFormatErrorParser implements JsonErrorParser {

    @Override
    public boolean canHandle(String errorMessage) {
        return errorMessage.contains("Cannot deserialize value") &&
               errorMessage.contains("LocalDateTime");
    }

    @Override
    public String parse(String errorMessage) {
        return "Invalid date format: use ISO-8601 format (e.g., '2031-01-15T17:00:00').";
    }

    @Override
    public int getPriority() {
        return 15;
    }
}
