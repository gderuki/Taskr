package com.gderuki.taskr.exception;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(Long id) {
        super("Comment not found with id: " + id);
    }

    public CommentNotFoundException(Long id, Long taskId) {
        super("Comment not found with id: " + id + " for task: " + taskId);
    }
}
