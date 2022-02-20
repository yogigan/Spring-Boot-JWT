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

import static org.springframework.http.HttpStatus.CREATED;
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
                ApiResponse.ok("Success retrieve users",
                        Collections.singletonMap("users", userService.findAll(page, size))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody @Valid AppUser user) {
        return ResponseEntity.status(CREATED).body(
                ApiResponse.ok("Success create user",
                                Collections.singletonMap("user", userService.saveUser(user))));
    }

}
