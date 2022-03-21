package com.example.spring.service;

import com.example.spring.exception.ApiConflictException;
import com.example.spring.exception.ApiNotFoundException;
import com.example.spring.model.domain.AppRole;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.response.UserInfoResponse;
import com.example.spring.repository.AppRoleRepository;
import com.example.spring.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.spring.model.domain.Role.ROLE_ADMIN;
import static com.example.spring.model.domain.Role.ROLE_USER;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppUserService implements UserDetailsService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user by username or email: {} .....", email);
        // check if user exists by email
        Optional<AppUser> appUser = appUserRepository.findByEmail(email);
        if (!appUser.isPresent()) {
            log.warn("User with email {} not found", email);

            // check if user exists by username
            appUser = appUserRepository.findByUsername(email);
            if (!appUser.isPresent()) {
                log.error("User with username or email {} not found", email);
                throw new AuthenticationCredentialsNotFoundException(String.format("User with username or email %s not found", email));
            }
        }

        log.info("success loading user by username or email: {}", email);
        return appUser.get();
    }

    public void setUserEnable(Long id) {
        log.info("Setting user enable => true: {}", id);
        AppUser appUser = findById(id);
        appUser.setIsEnabled(true);
        log.info("success setting user enable => true: {}", id);
        appUserRepository.save(appUser);
    }

    public AppUser findById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new ApiNotFoundException(
                            String.format("User with username or email %s not found", id));
                });
    }

    public void saveUser(AppUser appUser) {
        log.info("Saving user.....");
        // check if email is already exist
        appUserRepository.findByEmail(appUser.getEmail()).ifPresent(user -> {
            log.error(String.format("Email %s is already exist", appUser.getEmail()));
            throw new ApiConflictException(String.format("Email %s is already exist", appUser.getEmail()));
        });

        //check if username is already exist
        appUserRepository.findByUsername(appUser.getUsername()).ifPresent(user -> {
            log.error(String.format("Username %s is already exist", appUser.getUsername()));
            throw new ApiConflictException(String.format("Username %s is already exist", appUser.getUsername()));
        });

        // save user with encrypted password
        Optional<AppRole> appRole = appRoleRepository.findByName(ROLE_USER);
        List<AppRole> roles = appRole.map(Arrays::asList).orElseGet(Arrays::asList);
        appUser.setAppUserRoles(roles);
        appUser.setPassword(bCryptPasswordEncoder.encode(appUser.getPassword()));
        log.info("success saving user: {}", appUser);
        appUserRepository.save(appUser);
    }

    public AppUser findByUsername(String username) {
        log.info("Finding user by username: {}", username);
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User with username {} not found", username);
                    return new ApiNotFoundException(String.format("User with username %s not found", username));
                });
    }

    public UserInfoResponse getUserInfoByUsername(String username) {
        AppUser user = findByUsername(username);
        log.info("Finding user by username: {}", username);
        return UserInfoResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(user.getUsername())
                .email(user.getEmail())
                .roles(user.getAppUserRoles().stream().map(AppRole::getName).collect(Collectors.toList()))
                .build();
    }

    public List<AppUser> findAll(int page, int size) {
        log.info("Finding all users");
        Page<AppUser> appUsers = appUserRepository.findAll(PageRequest.of(page, size));
        if (appUsers.isEmpty()) {
            log.error("No user found");
            throw new ApiNotFoundException("No user found");
        }
        return appUsers.getContent();
    }

    public void saveAll(List<AppUser> appUsers) {
        log.info("Saving all users");
        appUserRepository.saveAll(appUsers);
    }

}
