package com.example.spring.service;

import com.example.spring.exception.ApiBadRequestException;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.ConfirmationToken;
import com.example.spring.model.requests.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AppUserService appUserService;
    @Mock
    private ConfirmationTokenService confirmationTokenService;
    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void testRegisterSuccess() {
        //given
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Yogi")
                .lastName("Pratama")
                .username("yogi")
                .email("mail@gmail.com")
                .password("toor")
                .build();

        //when
        String expected = registrationService.register(request);

        //then
        verify(appUserService, times(1))
                .saveUser(isA(AppUser.class));
        verify(confirmationTokenService, times(1))
                .saveConfirmationToken(isA(ConfirmationToken.class));
        assertThat(expected).isNotNull();

    }

    @Test
    void testRegisterEmailNotValid() {
        //given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Yogi")
                .lastName("Pratama")
                .username("yogi")
                .email("mail")
                .password("toor")
                .build();

        //when & then
        assertThatThrownBy(() -> registrationService.register(registerRequest))
                .hasMessage("Email %s is not valid", registerRequest.getEmail())
                .isInstanceOf(ApiBadRequestException.class);
        verify(appUserService, never())
                .saveUser(isA(AppUser.class));
        verify(confirmationTokenService, never())
                .saveConfirmationToken(isA(ConfirmationToken.class));
    }

    @Test
    void testConfirmRegistrationSuccess() {
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

        //when
        when(confirmationTokenService.findByToken(token)).thenReturn(confirmationToken);
        registrationService.confirmRegistration(token);

        //then
        verify(confirmationTokenService, times(1)).findByToken(token);
        verify(confirmationTokenService, times(1)).setConfirmedToken(token);
        verify(appUserService, times(1)).setUserEnable(appUser.getId());
    }

    @Test
    void testConfirmRegistrationTokenAlreadyConfirmed() {
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
                .confirmedAt(LocalDateTime.now())
                .build();

        //when
        when(confirmationTokenService.findByToken(token)).thenReturn(confirmationToken);

        //then
        assertThatThrownBy(() -> registrationService.confirmRegistration(token))
                .hasMessage("Token %s is already confirmed", token)
                .isInstanceOf(ApiBadRequestException.class);
        verify(confirmationTokenService, times(1)).findByToken(token);
        verify(confirmationTokenService, never()).setConfirmedToken(token);
        verify(appUserService, never()).setUserEnable(appUser.getId());
    }

    @Test
    void testConfirmRegistrationTokenIsExpired() {
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
                .expiresAt(LocalDateTime.now().minusMinutes(20))
                .build();

        //when
        when(confirmationTokenService.findByToken(token)).thenReturn(confirmationToken);

        //then
        assertThatThrownBy(() -> registrationService.confirmRegistration(token))
                .hasMessage("Token %s is expired", token)
                .isInstanceOf(ApiBadRequestException.class);
        verify(confirmationTokenService, times(1)).findByToken(token);
        verify(confirmationTokenService, never()).setConfirmedToken(token);
        verify(appUserService, never()).setUserEnable(appUser.getId());
    }

    @Test
    void testIsEmailValid() {
        //given
        String email = "mail@gmail.com";

        //when
        boolean isValid = registrationService.isEmailValid(email);

        //then
        assertThat(isValid).isTrue();
    }

    @Test
    void testIsEmailNotValid() {
        //given
        String email = "mail";

        //when
        boolean isValid = registrationService.isEmailValid(email);

        //then
        assertThat(isValid).isFalse();
    }
}