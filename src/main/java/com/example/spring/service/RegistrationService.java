package com.example.spring.service;

import com.example.spring.exception.ApiBadRequestException;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.ConfirmationToken;
import com.example.spring.model.requests.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    private final AppUserService appUserService;
    private final ConfirmationTokenService confirmationTokenService;
    private static final String EMAIL_NOT_VALID_MESSAGE = "Email is not valid";

    public String register(RegistrationRequest request) {
        // validate email
        if (!isEmailValid(request.getEmail())) {
            throw new ApiBadRequestException(EMAIL_NOT_VALID_MESSAGE);
        }

        // create user
        return appUserService.registerUser(
                AppUser.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(request.getPassword())
                        .build()

        );
    }

    public void confirmRegistration(String token) {
        // validate token
        ConfirmationToken confirmationToken = confirmationTokenService.findByConfirmationToken(token);

        // validate unconfirmed token
        if (confirmationToken.getConfirmedAt() != null) {
            throw new ApiBadRequestException("Token already confirmed");
        }

        // validate token expiration
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiBadRequestException("Token expired");
        }

        // set confirmedAt
        confirmationTokenService.setConfirmedToken(confirmationToken.getToken());

        // set user confirmed
        appUserService.setUserEnable(confirmationToken.getAppUser().getId());
    }

    public boolean isEmailValid(String email) {
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
