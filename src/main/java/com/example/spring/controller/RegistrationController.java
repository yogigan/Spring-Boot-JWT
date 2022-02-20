package com.example.spring.controller;

import com.example.spring.model.requests.RegistrationRequest;
import com.example.spring.model.response.ApiResponse;
import com.example.spring.service.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;

@RestController
@RequestMapping("/api/v1/registration")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid RegistrationRequest request) {
        String token = registrationService.register(request);
        return ResponseEntity.ok(
                ApiResponse.ok("User registered successfully",
                        Collections.singletonMap("confirmationToken", token)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> confirm(@RequestParam("token") String token) {
        registrationService.confirmRegistration(token);
        return ResponseEntity.ok(
                ApiResponse.ok("User confirmed successfully, you can now login"));
    }
}
