package com.gderuki.taskr.exception;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(Long id) {
        super("Tag not found with id: " + id);
    }

    public TagNotFoundException(String name) {
        super("Tag not found with name: " + name);
    }
}
