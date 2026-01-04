package com.gderuki.taskr.exception;

public class AttachmentNotFoundException extends RuntimeException {

    public AttachmentNotFoundException(String message) {
        super(message);
    }

    public AttachmentNotFoundException(Long id) {
        super("Attachment not found with id: " + id);
    }
}
