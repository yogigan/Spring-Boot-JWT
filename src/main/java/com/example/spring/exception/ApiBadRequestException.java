package com.example.spring.exception;

/**
 * @author Yogi
 * @since 07/02/2022
 */
public class ApiBadRequestException extends RuntimeException{

    public ApiBadRequestException(String message) {
        super(message);
    }
}
