package com.example.spring.controller;

import com.example.spring.model.domain.AppUser;
import com.example.spring.model.response.ApiResponse;
import com.example.spring.model.response.LoginResponse;
import com.example.spring.model.response.TokenResponse;
import com.example.spring.service.AppUserService;
import com.example.spring.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.springframework.http.HttpStatus.OK;

/**
 * @author Yogi
 * @since 15/02/2022
 */
@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
@Slf4j
public class SessionManagementController {

    private final AppUserService userService;
    private final JWTUtil jwtUtil;

    @GetMapping("/refresh-token")
    public ResponseEntity<ApiResponse> refreshToken(HttpServletRequest request) {
        try {
            String tokenFromRequest = jwtUtil.getTokenFromRequest(request);
            AppUser user = userService.findByUsername(jwtUtil.getUsernameByToken(tokenFromRequest));
            TokenResponse accessToken = jwtUtil.createAccessToken(user, request);
            TokenResponse refreshToken = jwtUtil.createRefreshToken(user, request);
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .status(HttpStatus.OK)
                            .message("Refresh token successfully")
                            .data(loginResponse)
                            .build());
        } catch (Exception e) {
            log.error("Error while refreshing token : {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .status(HttpStatus.BAD_REQUEST)
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/user-me")
    public ResponseEntity<ApiResponse> getUser(HttpServletRequest request) {
        String token = jwtUtil.getTokenFromRequest(request);
        String username = jwtUtil.getUsernameByToken(token);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .code(OK.value())
                        .status(OK)
                        .message("Success retrieve user")
                        .data(Collections.singletonMap("user", userService.getUserInfoByUsername(username)))
                        .build()
        );
    }
}
