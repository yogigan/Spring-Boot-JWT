package com.example.spring.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.spring.exception.ApiBadRequestException;
import com.example.spring.exception.ApiUnauthorizedException;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.response.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * @author Yogi
 * @since 09/02/2022 - 20:21
 */
@Slf4j
public class JWTUtils {

    public static final String SECRET = "secret";
    public static final String BEARER = "Bearer ";
    private static final Long ACCESS_TOKEN_EXPIRED = 30 * 60000L;//plus 30 minutes
    private static final Long REFRESH_TOKEN_EXPIRED = 30 * 24 * 3_600_000L;//plus 30 days

    public static String getTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            return authorizationHeader.substring(BEARER.length());
        }
        log.error("No token found in request");
        throw new ApiBadRequestException("Invalid token");
    }

    public static TokenResponse createAccessToken(AppUser user, HttpServletRequest request) {
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

    public static TokenResponse createRefreshToken(AppUser user, HttpServletRequest request) throws RuntimeException {
        return TokenResponse.builder()
                .value(JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRED))
                        .withIssuer(request.getRequestURI())
                        .sign(getAlgorithm()))
                .expiredAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRED))
                .build();
    }

    public static String getUsernameByToken(String token) {
        return getDecodedJWT(token).getSubject();
    }

    public static String[] getRolesByToken(String token) {
        return getDecodedJWT(token).getClaim("roles").asArray(String.class);
    }

    public static DecodedJWT getDecodedJWT(String token) {
        try {
            Algorithm algorithm = getAlgorithm();
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (TokenExpiredException e) {
            throw new ApiUnauthorizedException("Token expired");
        }
    }

    public static Algorithm getAlgorithm() {
        return Algorithm.HMAC256(SECRET);
    }
}
