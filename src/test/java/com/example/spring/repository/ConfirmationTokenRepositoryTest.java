package com.example.spring.repository;

import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.ConfirmationToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class ConfirmationTokenRepositoryTest {

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;
    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        confirmationTokenRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        appUserRepository.deleteAll();
    }

    @Test
    void testFindByTokenExists() {
        //given
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("yogi")
                .lastName("pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();
        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .appUser(appUser)
                .token(token)
                .confirmedAt(null)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(20))
                .build();
        confirmationTokenRepository.save(confirmationToken);

        //when
        Optional<ConfirmationToken> expected = confirmationTokenRepository.findByToken(token);

        //then
        assertThat(expected.isPresent()).isTrue();
        assertThat(expected).isNotEmpty();
        assertThat(expected.get().getToken()).isEqualTo(token);

    }

    @Test
    void testFindByTokenNotExists() {
        //given
        String token = UUID.randomUUID().toString();

        //when
        Optional<ConfirmationToken> expected = confirmationTokenRepository.findByToken(token);

        //then
        assertThat(expected.isPresent()).isFalse();
        assertThat(expected).isEmpty();
    }
}