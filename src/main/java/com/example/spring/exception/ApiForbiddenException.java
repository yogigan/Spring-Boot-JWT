package com.example.spring.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * @author Yogi
 * @since 07/02/2022
 */
public class ApiForbiddenException extends AccessDeniedException {

    public ApiForbiddenException(String message) {
        super(message);
    }

}
