package com.example.spring.controller;

import com.example.spring.model.domain.AppUser;
import com.example.spring.model.response.ApiResponse;
import com.example.spring.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;

import static org.springframework.http.HttpStatus.OK;

/**
 * @author Yogi
 * @since 07/02/2022
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final AppUserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                                   @RequestParam(value = "size", defaultValue = "5") int size) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .code(OK.value())
                        .status(OK)
                        .message("Success retrieve users")
                        .data(Collections.singletonMap("users", userService.findAll(page, size)))
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody @Valid AppUser user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/user").toUriString());
        return ResponseEntity.created(uri).body(
                ApiResponse.builder()
                        .code(OK.value())
                        .status(OK)
                        .message("Success create user")
                        .data(Collections.singletonMap("user", userService.saveUser(user)))
                        .build()
        );
    }

}
