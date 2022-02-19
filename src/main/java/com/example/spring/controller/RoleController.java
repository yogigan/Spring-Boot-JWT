package com.example.spring.controller;

import com.example.spring.model.response.ApiResponse;
import com.example.spring.model.domain.AppRole;
import com.example.spring.model.requests.RoleToUserRequest;
import com.example.spring.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Yogi
 * @since 08/02/2022
 */
@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
public class RoleController {

    private final AppUserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse> createRole(@RequestBody @Valid AppRole role) {
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/role").build().toUri();
        return ResponseEntity.created(uri).body(
                ApiResponse.builder()
                        .code(CREATED.value())
                        .status(CREATED)
                        .message("Role created successfully")
                        .data(Collections.singletonMap("role", userService.saveRole(role)))
                        .build());
    }


    @PostMapping("/add-to-user")
    public ResponseEntity<ApiResponse> addToUser(@RequestBody @Valid RoleToUserRequest roleToUserRequest) {
        userService.addRoleToUser(roleToUserRequest.getUsername(), roleToUserRequest.getRoleName());
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .code(OK.value())
                        .status(OK)
                        .message("Success add role to user")
                        .build()
        );
    }
}
