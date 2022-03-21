package com.example.spring.service;

import com.example.spring.exception.ApiBadRequestException;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.ConfirmationToken;
import com.example.spring.model.requests.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RegistrationService {

    @Value("${parameter.value.is-use-email-verification}")
    private boolean IS_USE_EMAIL_VERIFICATION;

    private final AppUserService appUserService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;

    public String register(RegisterRequest request) {
        log.info("Registering user: {}", request);

        // validate email
        if (!isEmailValid(request.getEmail())) {
            log.error("Email {} is not valid", request.getEmail());
            throw new ApiBadRequestException(String.format("Email %s is not valid", request.getEmail()));
        }

        // save user
        log.info("Saving user {}", request.getEmail());
        AppUser appUser = AppUser.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        appUserService.saveUser(appUser);

        // save confirmation token
        log.info("Save confirmation token for user {}", appUser.getUsername());
        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .appUser(appUser)
                .build();
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        // send email confirmation
        if (IS_USE_EMAIL_VERIFICATION) {
            log.info("Send email confirmation to {}", appUser.getEmail());
            emailService.sendRegistrationEmail(appUser, confirmationToken.getToken());
        }

        return confirmationToken.getToken();
    }

    public void confirmRegistration(String token) {
        // validate token
        ConfirmationToken confirmationToken = confirmationTokenService.findByToken(token);

        // validate unconfirmed token
        if (confirmationToken.getConfirmedAt() != null) {
            throw new ApiBadRequestException(String.format("Token %s is already confirmed", token));
        }

        // validate token expiration
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiBadRequestException(String.format("Token %s is expired", token));
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
