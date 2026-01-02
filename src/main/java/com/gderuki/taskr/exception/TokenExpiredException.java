package com.gderuki.taskr.exception;

public class TokenExpiredException extends RefreshTokenException {

    public TokenExpiredException(String message) {
        super(message);
    }
}
