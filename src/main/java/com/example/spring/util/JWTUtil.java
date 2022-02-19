package com.example.spring.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.spring.exception.ApiBadRequestException;
import com.example.spring.exception.ApiUnauthorizedException;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.response.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


/**
 * @author Yogi
 * @since 09/02/2022
 */
@Slf4j
@Component
public class JWTUtil {

    @Value("${parameter.value.jwt-secret}")
    private String SECRET;
    @Value("${parameter.value.jwt-bearer}")
    private String BEARER;
    @Value("${parameter.value.jwt-access-token-expiration-time}")
    private Long ACCESS_TOKEN_EXPIRED;
    @Value("${parameter.value.jwt-refresh-token-expiration-time}")
    private Long REFRESH_TOKEN_EXPIRED;

    public String getTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            return authorizationHeader.substring(BEARER.length());
        }
        log.error("No token found in request");
        throw new ApiBadRequestException("Invalid token");
    }

    public TokenResponse createAccessToken(AppUser user, HttpServletRequest request) {
        return TokenResponse.builder()
                .value(JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED))
                        .withIssuer(request.getRequestURI())
                        .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                        .sign(getAlgorithm()))
                .expiredAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED))
                .build();
    }

    public TokenResponse createRefreshToken(AppUser user, HttpServletRequest request) throws RuntimeException {
        return TokenResponse.builder()
                .value(JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRED))
                        .withIssuer(request.getRequestURI())
                        .sign(getAlgorithm()))
                .expiredAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRED))
                .build();
    }

    public String getUsernameByToken(String token) {
        return getDecodedJWT(token)
                .getSubject();
    }

    public String[] getRolesByToken(String token) {
        return getDecodedJWT(token)
                .getClaim("roles")
                .asArray(String.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = getDecodedJWT(token);
            if (decodedJWT.getExpiresAt().getTime() < new Date().getTime()) {
                log.error("Token expired");
                return true;
            }
        } catch (TokenExpiredException e) {
            log.error("Token is expired : {}", e.getMessage());
            return true;
        }
        return false;
    }

    public DecodedJWT getDecodedJWT(String token) {
        try {
            Algorithm algorithm = getAlgorithm();
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (TokenExpiredException e) {
            throw new ApiUnauthorizedException("Token expired : " + e.getMessage());
        } catch (Exception e) {
            throw new ApiBadRequestException("Token expired : " + e.getMessage());
        }
    }

    public Algorithm getAlgorithm() {
        return Algorithm.HMAC256(SECRET);
    }
}
