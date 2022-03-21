package com.example.spring.service;

import com.example.spring.exception.ApiBadRequestException;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.ConfirmationToken;
import com.example.spring.repository.ConfirmationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
    @InjectMocks
    private ConfirmationTokenService confirmationTokenService;

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
        ConfirmationToken expected = confirmationTokenService.findByToken(token);

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
        assertThatThrownBy(() -> confirmationTokenService.findByToken(token))
                .hasMessage("Invalid token: " + token)
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

        verify(confirmationTokenRepository, times(1))
                .findByToken(token);
        verify(confirmationTokenRepository, times(1))
                .save(confirmationTokenArgumentCaptor.capture());

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
                .hasMessage("Invalid token: " + token);
        verify(confirmationTokenRepository, times(1)).findByToken(token);
        verify(confirmationTokenRepository, never()).save(any());
    }

    @Test
    void testSaveConfirmationToken() {
        //given
        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .id(1L)
                .token(UUID.randomUUID().toString())
                .appUser(new AppUser())
                .expiresAt(LocalDateTime.now().plusMinutes(20))
                .build();

        //when
        confirmationTokenService.saveConfirmationToken(confirmationToken);

        //then
        ArgumentCaptor<ConfirmationToken> confirmationTokenArgumentCaptor =
                ArgumentCaptor.forClass(ConfirmationToken.class);
        verify(confirmationTokenRepository, times(1))
                .save(confirmationTokenArgumentCaptor.capture());
        ConfirmationToken value = confirmationTokenArgumentCaptor.getValue();
        assertThat(value).isNotNull();
        assertThat(value).isEqualTo(confirmationToken);

    }
}