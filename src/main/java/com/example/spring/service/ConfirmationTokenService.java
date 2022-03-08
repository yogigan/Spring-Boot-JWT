package com.example.spring.service;

import com.example.spring.exception.ApiBadRequestException;
import com.example.spring.model.domain.ConfirmationToken;
import com.example.spring.repository.ConfirmationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationToken findByConfirmationToken(String token) {
        return confirmationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiBadRequestException("Invalid token"));
    }

    public void setConfirmedToken(String token) {
        // set confirmed to true
        ConfirmationToken confirmationToken = findByConfirmationToken(token);
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);

    }
}
