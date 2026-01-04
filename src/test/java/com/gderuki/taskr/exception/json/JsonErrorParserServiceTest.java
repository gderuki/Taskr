package com.gderuki.taskr.exception.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Note: Jackson's JSON parser stops at the first error, so users will see
 * one error at a time.
 */
class JsonErrorParserServiceTest {

    private JsonErrorParserService service;

    @BeforeEach
    void setUp() {
        List<JsonErrorParser> parsers = List.of(
                new TrailingCommaErrorParser(),
                new MissingQuotesErrorParser(),
                new InvalidDateFormatErrorParser(),
                new DeserializationErrorParser(),
                new IncompleteJsonErrorParser(),
                new MissingValueErrorParser()
        );
        service = new JsonErrorParserService(parsers);
    }

    @Test
    void shouldDetectTrailingComma() {
        String error = "Unexpected character ('}' (code 125)): was expecting double-quote to start field name\n at [Source: REDACTED; line: 6, column: 1]";
        String result = service.parseError(error);
        assertThat(result).isEqualTo("Invalid JSON: trailing comma detected. Remove the comma after the last field.");
    }

    @Test
    void shouldDetectMissingQuotes() {
        String error = "Unexpected character ('u' (code 117)): was expecting double-quote to start field name\n at [Source: REDACTED; line: 1, column: 2]";
        String result = service.parseError(error);
        assertThat(result).isEqualTo("Invalid JSON: field names must be enclosed in double quotes.");
    }

    @Test
    void shouldDetectInvalidDateFormat() {
        String error = "Cannot deserialize value of type `java.time.LocalDateTime` from String \"invalid-date\"";
        String result = service.parseError(error);
        assertThat(result).isEqualTo("Invalid date format: use ISO-8601 format (e.g., '2031-01-15T17:00:00').");
    }

    @Test
    void shouldDetectDeserializationError() {
        String error = "Cannot deserialize value of type `java.lang.Integer` from String \"abc\"";
        String result = service.parseError(error);
        assertThat(result).isEqualTo("Invalid data format: check field types and values.");
    }

    @Test
    void shouldDetectIncompleteJson() {
        String error = "Unexpected end-of-input: expected close marker for Object";
        String result = service.parseError(error);
        assertThat(result).isEqualTo("Invalid JSON: incomplete JSON structure, missing closing brace or bracket.");
    }

    @Test
    void shouldReturnDefaultMessageForUnknownError() {
        String error = "Some completely unknown error";
        String result = service.parseError(error);
        assertThat(result).isEqualTo("Invalid JSON: request body is missing or malformed");
    }

    @Test
    void shouldHandleNullMessage() {
        String result = service.parseError(null);
        assertThat(result).isEqualTo("Invalid JSON: request body is missing or malformed");
    }

    @Test
    void shouldPrioritizeTrailingCommaOverMissingQuotes() {
        String error = "was expecting double-quote to start field name and Unexpected character ('}";
        String result = service.parseError(error);
        assertThat(result).isEqualTo("Invalid JSON: trailing comma detected. Remove the comma after the last field.");
    }
}
