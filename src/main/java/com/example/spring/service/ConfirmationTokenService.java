package com.example.spring.service;

import com.example.spring.model.domain.ConfirmationToken;
import com.example.spring.repository.ConfirmationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public Optional<ConfirmationToken> findByConfirmationToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public void setConfirmed(String token) {
        // TODO: set confirmed to true
        confirmationTokenRepository
                .findByToken(token)
                .ifPresent(confirmationToken -> {
                    confirmationToken.setConfirmedAt(LocalDateTime.now());
                    confirmationTokenRepository.save(confirmationToken);

                });

    }
}
