package com.example.spring.controller;

import com.example.spring.model.requests.LoginRequest;
import com.example.spring.model.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yogi
 * @since 16/02/2022
 */
@RestController
@RequestMapping("/api/v1/login")
public class LoginController {

    @PostMapping
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .message("Login Successful")
                .build()
        );
    }

}
