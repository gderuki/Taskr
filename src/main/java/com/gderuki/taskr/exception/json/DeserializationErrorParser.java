package com.gderuki.taskr.exception.json;

import org.springframework.stereotype.Component;

/**
 * Parser for general deserialization errors
 */
@Component
public class DeserializationErrorParser implements JsonErrorParser {

    @Override
    public boolean canHandle(String errorMessage) {
        return errorMessage.contains("Cannot deserialize");
    }

    @Override
    public String parse(String errorMessage) {
        return "Invalid data format: check field types and values.";
    }

    @Override
    public int getPriority() {
        return 50;
    }
}
