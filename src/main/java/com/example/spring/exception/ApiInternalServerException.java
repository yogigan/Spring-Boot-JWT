package com.example.spring.exception;

/**
 * @author Yogi
 * @since 07/02/2022
 */
public class ApiInternalServerException extends RuntimeException {

    public ApiInternalServerException(String message) {
        super(message);
    }
}
