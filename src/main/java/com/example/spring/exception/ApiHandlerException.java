package com.example.spring.exception;

import com.example.spring.model.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

/**
 * @author Yogi
 * @since 07/02/2022
 */
@RestControllerAdvice
@Slf4j
public class ApiHandlerException {

    @ExceptionHandler(ApiNotFoundException.class)
    public ResponseEntity<ApiResponse> notFound(ApiNotFoundException e) {
        return setResponse(NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ApiConflictException.class)
    public ResponseEntity<ApiResponse> conflict(ApiConflictException e) {
        return setResponse(CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ApiBadRequestException.class)
    public ResponseEntity<ApiResponse> badRequest(ApiBadRequestException e) {
        return setResponse(BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ApiInternalServerException.class)
    public ResponseEntity<ApiResponse> internalServer(ApiInternalServerException e) {
        return setResponse(INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(ApiUnauthorizedException.class)
    public ResponseEntity<ApiResponse> unauthorized(ApiUnauthorizedException e) {
        return setResponse(UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ApiForbiddenException.class)
    public ResponseEntity<ApiResponse> forbidden(ApiForbiddenException e) {
        return setResponse(FORBIDDEN, e.getMessage());
    }

    public ResponseEntity<ApiResponse> setResponse(HttpStatus status, String message) {
        log.info("{} - {}", status, message);
        return ResponseEntity.status(status).body(
                ApiResponse.builder()
                        .code(status.value())
                        .status(status.name())
                        .message(message)
                        .build()
        );
    }

}
