package com.example.spring.exception;

/**
 * @author Yogi
 * @since 07/02/2022
 */
public class ApiNotFoundException extends RuntimeException {

    public ApiNotFoundException(String message) {
        super(message);
    }
}
