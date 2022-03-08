package com.example.spring.service;

import com.example.spring.exception.ApiBadRequestException;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.ConfirmationToken;
import com.example.spring.repository.AppUserRepository;
import com.example.spring.repository.ConfirmationTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationTokenServiceTest {

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;
    @Mock
    private AppUserRepository appUserRepository;
    private ConfirmationTokenService confirmationTokenService;

    @BeforeEach
    void setUp() {
        confirmationTokenService = new ConfirmationTokenService(confirmationTokenRepository);
        confirmationTokenRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void testFindByConfirmationTokenExists() {
        //given
        String token = UUID.randomUUID().toString();
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();

        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .id(1L)
                .token(token)
                .appUser(appUser)
                .expiresAt(LocalDateTime.now().plusMinutes(20))
                .build();
        given(confirmationTokenRepository.findByToken(token)).willReturn(Optional.of(confirmationToken));

        //when
        ConfirmationToken expected = confirmationTokenService.findByConfirmationToken(token);

        //then
        verify(confirmationTokenRepository, times(1)).findByToken(token);
        assertThat(expected).isNotNull();
        assertThat(expected).isEqualTo(confirmationToken);
    }

    @Test
    void testFindByConfirmationTokenNotExists() {
        //given
        String token = UUID.randomUUID().toString();
        given(confirmationTokenRepository.findByToken(token)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> confirmationTokenService.findByConfirmationToken(token))
                .hasMessage("Invalid token")
                .isInstanceOf(ApiBadRequestException.class);
        verify(confirmationTokenRepository, times(1)).findByToken(token);
    }

    @Test
    void testSuccessSetConfirmedToken() {
        //given
        String token = UUID.randomUUID().toString();
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();

        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .id(1L)
                .token(token)
                .appUser(appUser)
                .expiresAt(LocalDateTime.now().plusMinutes(20))
                .build();
        given(confirmationTokenRepository.findByToken(token)).willReturn(Optional.of(confirmationToken));

        //when
        confirmationTokenService.setConfirmedToken(token);

        //then
        ArgumentCaptor<ConfirmationToken> confirmationTokenArgumentCaptor =
                ArgumentCaptor.forClass(ConfirmationToken.class);

        verify(confirmationTokenRepository, times(1)).findByToken(token);
        verify(confirmationTokenRepository, times(1)).save(confirmationTokenArgumentCaptor.capture());

        ConfirmationToken value = confirmationTokenArgumentCaptor.getValue();
        assertThat(value).isNotNull();
        assertThat(value.getConfirmedAt()).isNotNull();
    }

    @Test
    void testFailedSetConfirmedTokenNotExist() {
        //given
        String token = UUID.randomUUID().toString();
        given(confirmationTokenRepository.findByToken(token)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> confirmationTokenService.setConfirmedToken(token))
                .isInstanceOf(ApiBadRequestException.class)
                .hasMessage("Invalid token");
        verify(confirmationTokenRepository, times(1)).findByToken(token);
        verify(confirmationTokenRepository, never()).save(any());
    }
}