package com.gderuki.taskr.exception.json;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Service implementation that coordinates JSON error parsing via registered parsers
 * <p>
 * Note: Jackson only reports one parsing error at a time, so we select
 * the most specific/helpful parser based on priority.
 */
@Service
@RequiredArgsConstructor
public class JsonErrorParserService implements JsonErrorParserServiceInterface {

    private final List<JsonErrorParser> parsers;

    @Override
    public String parseError(String errorMessage) {
        if (errorMessage == null) {
            return "Invalid JSON: request body is missing or malformed";
        }

        return parsers.stream()
                .sorted(Comparator.comparingInt(JsonErrorParser::getPriority))
                .filter(parser -> parser.canHandle(errorMessage))
                .findFirst()
                .map(parser -> parser.parse(errorMessage))
                .orElse("Invalid JSON: request body is missing or malformed");
    }
}
