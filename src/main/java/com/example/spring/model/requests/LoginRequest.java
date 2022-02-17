package com.example.spring.model.requests;

import lombok.Data;

/**
 * @author Yogi
 * @since 09/02/2022
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
