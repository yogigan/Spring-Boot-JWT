package com.example.spring.service;

import com.example.spring.exception.ApiBadRequestException;
import com.example.spring.model.domain.ConfirmationToken;
import com.example.spring.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationToken findByToken(String token) {
        log.info("find confirmation token by token: {}", token);
        return confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.error("Confirmation token not found");
                    return new ApiBadRequestException("Invalid token: " + token);
                });
    }

    public void setConfirmedToken(String token) {
        // set confirmed to true
        log.info("set confirmed to true for token: {}", token);
        ConfirmationToken confirmationToken = findByToken(token);
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
    }

    public void saveConfirmationToken(ConfirmationToken confirmationToken) {
        log.info("save confirmation token: {}", confirmationToken);
        confirmationTokenRepository.save(confirmationToken);
    }
}
