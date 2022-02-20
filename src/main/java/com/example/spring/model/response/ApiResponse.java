package com.example.spring.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

import java.util.Date;

/**
 * @author Yogi
 * @since 06/02/2022
 */
@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Asia/Jakarta")
    private Date timestamp = new Date();
    private Integer code;
    private String status;
    private String message;
    private Object data;
    private String error;

    public static ApiResponse ok(String message, Object data) {
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.name())
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse ok(String message) {
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.name())
                .message(message)
                .build();
    }

    public static ApiResponse created(String message, Object data) {
        return ApiResponse.builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED.name())
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse created(String message) {
        return ApiResponse.builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED.name())
                .message(message)
                .build();
    }

    public static ApiResponse badRequest(String message) {
        return ApiResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.name())
                .message(message)
                .build();
    }

    public static ApiResponse unauthorized(String message) {
        return ApiResponse.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .status(HttpStatus.UNAUTHORIZED.name())
                .message(message)
                .build();
    }

    public static ApiResponse forbidden(String message, Object data) {
        return ApiResponse.builder()
                .code(HttpStatus.FORBIDDEN.value())
                .status(HttpStatus.FORBIDDEN.name())
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse notFound(String message) {
        return ApiResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .status(HttpStatus.NOT_FOUND.name())
                .message(message)
                .build();
    }

    public static ApiResponse internalServerError(String message, Object data) {
        return ApiResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse conflict(String message, Object data) {
        return ApiResponse.builder()
                .code(HttpStatus.CONFLICT.value())
                .status(HttpStatus.CONFLICT.name())
                .message(message)
                .data(data)
                .build();
    }


}
