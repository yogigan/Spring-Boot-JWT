package com.example.spring.model.requests;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RegistrationRequest {

    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String username;
    @NotNull
    private String email;
    @NotNull
    private String password;
}
