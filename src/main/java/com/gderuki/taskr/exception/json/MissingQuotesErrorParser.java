package com.gderuki.taskr.exception.json;

import org.springframework.stereotype.Component;

@Component
public class MissingQuotesErrorParser implements JsonErrorParser {

    @Override
    public boolean canHandle(String errorMessage) {
        return errorMessage.contains("was expecting double-quote to start field name");
    }

    @Override
    public String parse(String errorMessage) {
        return "Invalid JSON: field names must be enclosed in double quotes.";
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
