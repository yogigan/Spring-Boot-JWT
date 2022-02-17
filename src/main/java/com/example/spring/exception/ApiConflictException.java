package com.example.spring.exception;

/**
 * @author Yogi
 * @since 07/02/2022
 */
public class ApiConflictException extends RuntimeException {

    public ApiConflictException(String message) {
        super(message);
    }
}
