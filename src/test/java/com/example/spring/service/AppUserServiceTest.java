package com.example.spring.service;

import com.example.spring.exception.ApiConflictException;
import com.example.spring.exception.ApiNotFoundException;
import com.example.spring.model.domain.AppRole;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.Role;
import com.example.spring.model.response.UserInfoResponse;
import com.example.spring.repository.AppRoleRepository;
import com.example.spring.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private AppRoleRepository appRoleRepository;
    @InjectMocks
    private AppUserService appUserService;

    @Test
    void testLoadUserByUsernameSuccessFoundEmail() {
        //given
        String email = "mail@gmail.com";
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();
        given(appUserRepository.findByEmail(email)).willReturn(Optional.of(appUser));

        //when
        UserDetails expected = appUserService.loadUserByUsername(email);

        //then
        verify(appUserRepository, times(1)).findByEmail(email);
        verify(appUserRepository, never()).findByUsername(email);
        assertEquals(expected, appUser);
    }

    @Test
    void testLoadUserByUsernameSuccessFoundUsername() {
        //given
        String email = "mail@gmail.com";
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();
        given(appUserRepository.findByEmail(email)).willReturn(Optional.empty());
        given(appUserRepository.findByUsername(email)).willReturn(Optional.of(appUser));

        //when
        UserDetails expected = appUserService.loadUserByUsername(email);

        //then
        verify(appUserRepository, times(1)).findByEmail(email);
        verify(appUserRepository, times(1)).findByUsername(email);
        assertEquals(expected, appUser);
    }

    @Test
    void testLoadUserByUsernameNotExists() {
        //given
        String email = "mail@gmail.com";
        given(appUserRepository.findByEmail(email)).willReturn(Optional.empty());
        given(appUserRepository.findByUsername(email)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> appUserService.loadUserByUsername(email))
                .hasMessage("User with username or email %s not found", email)
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
        verify(appUserRepository, times(1)).findByEmail(email);
        verify(appUserRepository, times(1)).findByUsername(email);

    }

    @Test
    void testSetUserEnable() {
        //given
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .isEnabled(false)
                .build();
        given(appUserRepository.findById(appUser.getId())).willReturn(Optional.of(appUser));

        //when
        appUserService.setUserEnable(appUser.getId());

        //then
        verify(appUserRepository, times(1)).findById(appUser.getId());
        ArgumentCaptor<AppUser> argumentCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository, times(1)).save(argumentCaptor.capture());
        AppUser value = argumentCaptor.getValue();
        assertThat(value.getId()).isEqualTo(appUser.getId());
        assertThat(value.isEnabled()).isTrue();

    }

    @Test
    void testFindByIdExists() {
        //given
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();
        given(appUserRepository.findById(appUser.getId())).willReturn(Optional.of(appUser));

        //when
        AppUser expected = appUserService.findById(appUser.getId());

        //then
        verify(appUserRepository, times(1)).findById(appUser.getId());
        assertEquals(expected, appUser);
    }

    @Test
    void testFindByIdNotExists() {
        //given
        Long id = 1L;
        given(appUserRepository.findById(id)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> appUserService.findById(id))
                .hasMessage("User with username or email %s not found", id)
                .isInstanceOf(ApiNotFoundException.class);
        verify(appUserRepository, times(1)).findById(id);

    }

    @Test
    void testSaveUserSuccess() {
        //given
        String bcryptedPassword = "$2a$10$LU6VqxccBFMpCu9WTE7tAOWIwRBN9Uu9J0iqMMhSCI1SXVclFA2UW";
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();
        given(bCryptPasswordEncoder.encode(appUser.getPassword()))
                .willReturn(bcryptedPassword);

        //when
        appUserService.saveUser(appUser);

        // then
        verify(appUserRepository, times(1)).findByEmail(appUser.getEmail());
        verify(appUserRepository, times(1)).findByUsername(appUser.getUsername());
        verify(appRoleRepository, times(1)).findByName(Role.ROLE_USER);
        verify(bCryptPasswordEncoder, times(1)).encode(isA(String.class));
        ArgumentCaptor<AppUser> argumentCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository, times(1)).save(argumentCaptor.capture());
        AppUser value = argumentCaptor.getValue();
        assertThat(value.getId()).isEqualTo(appUser.getId());
        assertThat(value.getPassword()).isEqualTo(bcryptedPassword);
    }

    @Test
    void testSaveUserEmailAlreadyExists() {
        //given
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();
        given(appUserRepository.findByEmail(appUser.getEmail())).willReturn(Optional.of(appUser));

        //when & then
        assertThatThrownBy(() -> appUserService.saveUser(appUser))
                .hasMessage("Email %s is already exist", appUser.getEmail())
                .isInstanceOf(ApiConflictException.class);
        verify(appUserRepository, times(1)).findByEmail(appUser.getEmail());
        verify(appUserRepository, never()).findByUsername(appUser.getUsername());
        verify(appUserRepository, never()).findByUsername(appUser.getUsername());
        verify(bCryptPasswordEncoder, never()).encode(isA(String.class));
        verify(appUserRepository, never()).save(appUser);
    }

    @Test
    void testSaveUserUsernameAlreadyExists() {
        //given
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();
        given(appUserRepository.findByUsername(appUser.getUsername()))
                .willReturn(Optional.of(appUser));

        //when & then
        assertThatThrownBy(() -> appUserService.saveUser(appUser))
                .hasMessage("Username %s is already exist", appUser.getUsername())
                .isInstanceOf(ApiConflictException.class);
        verify(appUserRepository, times(1)).findByEmail(appUser.getEmail());
        verify(appUserRepository, times(1)).findByUsername(appUser.getUsername());
        verify(appRoleRepository, never()).findByName(Role.ROLE_USER);
        verify(bCryptPasswordEncoder, never()).encode(isA(String.class));
        verify(appUserRepository, never()).save(appUser);
    }

    @Test
    void testFindByUsernameExists() {
        //given
        String username = "yogi";
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username(username)
                .password("toor")
                .build();
        given(appUserRepository.findByUsername(username))
                .willReturn(Optional.of(appUser));

        //when
        AppUser expected = appUserService.findByUsername(username);

        //then
        verify(appUserRepository, times(1)).findByUsername(username);
        assertThat(expected).isEqualTo(appUser);
    }

    @Test
    void testFindByUsernameNotExists() {
        //given
        String username = "yogi";
        given(appUserRepository.findByUsername(username))
                .willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> appUserService.findByUsername(username))
                .hasMessage("User with username %s not found", username)
                .isInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void testGetUserInfoByUsernameExists() {
        //given
        String username = "yogi";
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username(username)
                .password("toor")
                .build();
        given(appUserRepository.findByUsername(username))
                .willReturn(Optional.of(appUser));
        UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                .firstName(appUser.getFirstName())
                .lastName(appUser.getLastName())
                .userName(appUser.getUsername())
                .email(appUser.getEmail())
                .roles(appUser.getAppUserRoles().stream().map(AppRole::getName).collect(Collectors.toList()))
                .build();

        //when
        UserInfoResponse expected = appUserService.getUserInfoByUsername(username);

        //then
        verify(appUserRepository, times(1)).findByUsername(username);
        assertThat(expected).isEqualTo(userInfoResponse);
    }

    @Test
    void testFindAllExist() {
        //given
        List<AppUser> appUsers = Arrays.asList(
                AppUser.builder()
                        .id(1L)
                        .firstName("Yogi")
                        .lastName("Pratama")
                        .email("yogi@gmail.com")
                        .username("yogi")
                        .password("toor")
                        .build(),
                AppUser.builder()
                        .id(2L)
                        .firstName("John")
                        .lastName("Doe")
                        .email("john@gmail.com")
                        .username("john")
                        .password("toor")
                        .build(),
                AppUser.builder()
                        .id(3L)
                        .firstName("James")
                        .lastName("Smith")
                        .email("james@gmail.com")
                        .username("jamessmith")
                        .password("toor")
                        .build()
        );
        int page = 0;
        int size = 3;
        given(appUserRepository.findAll(PageRequest.of(page, size)))
                .willReturn(new PageImpl<>(appUsers));

        //when
        List<AppUser> expected = appUserService.findAll(page, size);

        //then
        verify(appUserRepository, times(1)).findAll(PageRequest.of(page, size));
        assertThat(expected).isEqualTo(appUsers);
        assertThat(expected.size()).isEqualTo(size);
    }

    @Test
    void testFindAllNotExist() {
        //given
        int page = 0;
        int size = 3;
        given(appUserRepository.findAll(PageRequest.of(page, size)))
                .willReturn(Page.empty());

        //when & then
        assertThatThrownBy(() -> appUserService.findAll(page, size))
                .hasMessage("No user found")
                .isInstanceOf(ApiNotFoundException.class);
        verify(appUserRepository, times(1)).findAll(PageRequest.of(page, size));
    }

    @Test
    void testSaveAll() {
        //given
        List<AppUser> appUsers = Arrays.asList(
                AppUser.builder()
                        .id(1L)
                        .firstName("Yogi")
                        .lastName("Pratama")
                        .email("yogi@gmail.com")
                        .username("yogi")
                        .password("toor")
                        .build(),
                AppUser.builder()
                        .id(2L)
                        .firstName("John")
                        .lastName("Doe")
                        .email("john@gmail.com")
                        .username("john")
                        .password("toor")
                        .build(),
                AppUser.builder()
                        .id(3L)
                        .firstName("James")
                        .lastName("Smith")
                        .email("james@gmail.com")
                        .username("jamessmith")
                        .password("toor")
                        .build()
        );
        given(appUserRepository.saveAll(appUsers)).willReturn(appUsers);

        //when
        appUserService.saveAll(appUsers);

        //then
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(appUserRepository, times(1)).saveAll(captor.capture());
        assertThat(captor.getValue()).isEqualTo(appUsers);
    }
}