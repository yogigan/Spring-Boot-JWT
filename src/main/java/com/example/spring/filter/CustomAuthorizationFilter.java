package com.example.spring.filter;

import com.example.spring.model.response.ApiResponse;
import com.example.spring.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Yogi
 * @since 15/02/2022
 */
@Slf4j
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private JWTUtil jwtUtil;
    private static final String[] EXCLUDE_PATH = {
            "/api/v1/login",
            "/api/v1/registration",
            "/api/v1/session",
            "/swagger-ui",
            "/swagger-resources",
            "/v2/api-docs",
            "/v3/api-docs",
            "/favicon.ico"
    };

    @Autowired
    public void setJwtUtil(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isPathExclude(request.getRequestURI())) {
            filterChain.doFilter(request, response);
        } else {
            try {
                String accessToken = jwtUtil.getTokenFromRequest(request);
                String username = jwtUtil.getUsernameByToken(accessToken);
                String[] roles = jwtUtil.getRolesByToken(accessToken);
                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                Arrays.stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
                log.info("Authentication success : {}", username);
            } catch (Exception e) {
                log.info("Authentication failed : {}", e.getMessage());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setHeader("Content-Type", "application/json;charset=UTF-8");
                response.getWriter().write(
                        new ObjectMapper().writeValueAsString(
                                ApiResponse.unauthorized(e.getMessage())
                        ));
                response.flushBuffer();
            }
        }
    }

    public boolean isPathExclude(String path) {
        for (String excluded : EXCLUDE_PATH) {
            if (path.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }
}
