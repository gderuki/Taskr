package com.gderuki.taskr.exception.json;

import org.springframework.stereotype.Component;

@Component
public class TrailingCommaErrorParser implements JsonErrorParser {

    @Override
    public boolean canHandle(String errorMessage) {
        return errorMessage.contains("was expecting double-quote to start field name") &&
               errorMessage.contains("Unexpected character ('}");
    }

    @Override
    public String parse(String errorMessage) {
        return "Invalid JSON: trailing comma detected. Remove the comma after the last field.";
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
