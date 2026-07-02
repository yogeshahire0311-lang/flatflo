package com.flatflow.search;

/** Thrown when a search request has invalid input; maps to HTTP 400 (see {@link SearchExceptionHandler}). */
public class InvalidSearchException extends RuntimeException {

    private final String error;

    public InvalidSearchException(String error, String message) {
        super(message);
        this.error = error;
    }

    public String error() {
        return error;
    }
}
