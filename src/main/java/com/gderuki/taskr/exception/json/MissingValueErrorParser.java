package com.gderuki.taskr.exception.json;

import org.springframework.stereotype.Component;

@Component
public class MissingValueErrorParser implements JsonErrorParser {

    @Override
    public boolean canHandle(String errorMessage) {
        return errorMessage.contains("Unexpected character ('}");
    }

    @Override
    public String parse(String errorMessage) {
        return "Invalid JSON: missing value after colon or trailing comma.";
    }

    @Override
    public int getPriority() {
        return 40;
    }
}
