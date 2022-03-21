package com.example.spring.service;

import com.example.spring.exception.ApiConflictException;
import com.example.spring.exception.ApiNotFoundException;
import com.example.spring.model.domain.AppRole;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.Role;
import com.example.spring.repository.AppRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppRoleServiceTest {

    @Mock
    private AppRoleRepository appRoleRepository;
    @Mock
    private AppUserService appUserService;
    @InjectMocks
    private AppRoleService appRoleService;

    @Test
    void testSaveRoleSuccess() {
        //given
        Role role = Role.ROLE_USER;
        AppRole appRole = AppRole.builder()
                .id(1L)
                .name(role)
                .build();
        given(appRoleRepository.findByName(role)).willReturn(Optional.empty());
        given(appRoleRepository.save(appRole)).willReturn(appRole);

        //when
        appRoleService.saveRole(appRole);

        //then
        verify(appRoleRepository, times(1)).findByName(role);
        ArgumentCaptor<AppRole> argumentCaptor = ArgumentCaptor.forClass(AppRole.class);
        verify(appRoleRepository).save(argumentCaptor.capture());
        AppRole result = argumentCaptor.getValue();
        assertEquals(appRole, result);
    }

    @Test
    void testSaveRoleAlreadyExists() {
        //given
        Role role = Role.ROLE_USER;
        AppRole appRole = AppRole.builder()
                .id(1L)
                .name(role)
                .build();
        given(appRoleRepository.findByName(role)).willReturn(Optional.of(appRole));

        //when & then
        assertThatThrownBy(() -> appRoleService.saveRole(appRole))
                .hasMessage("Role %s already exists", role)
                .isInstanceOf(ApiConflictException.class);
        verify(appRoleRepository, times(1)).findByName(role);
        verify(appRoleRepository, times(0)).save(appRole);
    }

    @Test
    void testAddRoleToUserSuccess() {
        //given
        String username = "yogi";
        Role role = Role.ROLE_USER;
        AppRole appRole = AppRole.builder()
                .id(1L)
                .name(role)
                .build();
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();
        given(appUserService.findByUsername(username)).willReturn(appUser);
        given(appRoleRepository.findByName(role)).willReturn(Optional.of(appRole));

        //when
        appRoleService.addRoleToUser(username, role);

        //then
        verify(appUserService, times(1)).findByUsername(username);
        verify(appRoleRepository, times(1)).findByName(role);

    }
    @Test
    void testAddRoleToUserButRoleNotExists() {
        //given
        String username = "yogi";
        Role role = Role.ROLE_USER;
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .build();
        given(appUserService.findByUsername(username)).willReturn(appUser);
        given(appRoleRepository.findByName(role)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> appRoleService.addRoleToUser(username, role))
                .hasMessage("Role %s not found", role)
                .isInstanceOf(ApiNotFoundException.class);
        verify(appUserService, times(1)).findByUsername(username);
        verify(appRoleRepository, times(1)).findByName(role);

    }
    @Test
    void testAddRoleToUserButUserAlreadyHasRole() {
        //given
        String username = "yogi";
        Role role = Role.ROLE_USER;
        AppRole appRole = AppRole.builder()
                .id(1L)
                .name(role)
                .build();
        AppUser appUser = AppUser.builder()
                .id(1L)
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username("yogi")
                .password("toor")
                .appUserRoles(Collections.singletonList(appRole))
                .build();
        given(appUserService.findByUsername(username)).willReturn(appUser);
        given(appRoleRepository.findByName(role)).willReturn(Optional.of(appRole));

        //when & then
        assertThatThrownBy(() -> appRoleService.addRoleToUser(username, role))
                .hasMessage("User %s already has role %s", username, role)
                .isInstanceOf(ApiConflictException.class);
        verify(appUserService, times(1)).findByUsername(username);
        verify(appRoleRepository, times(1)).findByName(role);

    }
}