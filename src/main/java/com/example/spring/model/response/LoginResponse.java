package com.example.spring.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * @author Yogi
 * @since 09/02/2022
 */
@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class LoginResponse {
    private TokenResponse accessToken;
    private TokenResponse refreshToken;
}
