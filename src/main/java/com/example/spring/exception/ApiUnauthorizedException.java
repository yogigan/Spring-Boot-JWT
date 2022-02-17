package com.example.spring.exception;

/**
 * @author Yogi
 * @since 07/02/2022
 */
public class ApiUnauthorizedException extends RuntimeException {

    public ApiUnauthorizedException(String message) {
        super(message);
    }
}
