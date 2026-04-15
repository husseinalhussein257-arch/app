package com.uniquest.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an operation conflicts with current state.
 * Examples:
 *   - Attempting to re-submit an already SUBMITTED attempt
 *   - Creating a branch/year with a name that already exists
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
