package com.example.spring.filter;

import com.example.spring.model.domain.AppUser;
import com.example.spring.model.requests.LoginRequest;
import com.example.spring.model.response.ApiResponse;
import com.example.spring.model.response.LoginResponse;
import com.example.spring.model.response.TokenResponse;
import com.example.spring.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

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
@Component
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private JWTUtil jwtUtil;
    private String username;
    private String password;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setJwtUtil(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

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
        TokenResponse accessToken = jwtUtil.createAccessToken(user, request);
        TokenResponse refreshToken = jwtUtil.createRefreshToken(user, request);
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(ApiResponse.ok("Login Successful",
                        LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build())
                ));
        response.flushBuffer();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.info("Login failed : {}", failed.getMessage());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(ApiResponse.badRequest("Login Failed, " + failed.getMessage())
        ));
        response.flushBuffer();
    }
}
