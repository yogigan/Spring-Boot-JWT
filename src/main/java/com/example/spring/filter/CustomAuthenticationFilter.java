package com.example.spring.filter;

import com.example.spring.model.domain.AppUser;
import com.example.spring.model.requests.LoginRequest;
import com.example.spring.model.response.ApiResponse;
import com.example.spring.model.response.LoginResponse;
import com.example.spring.model.response.TokenResponse;
import com.example.spring.util.JWTUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Yogi
 * @since 08/02/2022
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private String username;
    private String password;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (request.getHeader("Content-Type").equals("application/json")) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                //json transformation
                ObjectMapper mapper = new ObjectMapper();
                LoginRequest loginRequest = mapper.readValue(stringBuilder.toString(), LoginRequest.class);

                this.username = loginRequest.getUsername();
                this.password = loginRequest.getPassword();
                log.info("username: {}, password: {}", username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        log.info("Login success : {}", authResult.getPrincipal());
        AppUser user = (AppUser) authResult.getPrincipal();
        TokenResponse accessToken = JWTUtils.createAccessToken(user, request);
        TokenResponse refreshToken = JWTUtils.createRefreshToken(user, request);
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(ApiResponse.builder()
                        .code(HttpStatus.OK.value())
                        .status(HttpStatus.OK)
                        .message("Login Successful")
                        .data(LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build())
                        .build()
                ));
        response.flushBuffer();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.info("Login failed : {}", failed.getMessage());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(ApiResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST)
                .message("Login Failed, " + failed.getMessage())
                .build()
        ));
        response.flushBuffer();
    }
}
