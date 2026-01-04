package com.gderuki.taskr.exception.json;

import org.springframework.stereotype.Component;

/**
 * Parser for incomplete JSON structure errors
 */
@Component
public class IncompleteJsonErrorParser implements JsonErrorParser {

    @Override
    public boolean canHandle(String errorMessage) {
        return errorMessage.contains("Unexpected end-of-input");
    }

    @Override
    public String parse(String errorMessage) {
        return "Invalid JSON: incomplete JSON structure, missing closing brace or bracket.";
    }

    @Override
    public int getPriority() {
        return 30;
    }
}
