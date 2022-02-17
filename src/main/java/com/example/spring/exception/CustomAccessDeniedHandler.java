package com.example.spring.exception;

import com.example.spring.model.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Yogi
 * @since 16/02/2022
 */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("Access Denied : {}", accessDeniedException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(
                        ApiResponse.builder()
                                .code(HttpStatus.UNAUTHORIZED.value())
                                .status(HttpStatus.UNAUTHORIZED)
                                .message(accessDeniedException.getMessage())
                                .build()
                ));
        response.flushBuffer();
    }
}
