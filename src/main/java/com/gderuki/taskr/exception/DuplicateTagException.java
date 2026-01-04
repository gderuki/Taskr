package com.gderuki.taskr.exception;

public class DuplicateTagException extends RuntimeException {
    public DuplicateTagException(String name) {
        super("Tag already exists with name: " + name);
    }
}
