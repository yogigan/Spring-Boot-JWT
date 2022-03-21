package com.example.spring.service;

import com.example.spring.exception.ApiConflictException;
import com.example.spring.exception.ApiNotFoundException;
import com.example.spring.model.domain.AppRole;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.Role;
import com.example.spring.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppRoleService {

    private final AppRoleRepository appRoleRepository;
    private final AppUserService appUserService;


    public void saveRole(AppRole appRole) {
        appRoleRepository.findByName(appRole.getName()).ifPresent(role -> {
            log.error("Role with name {} already exist", appRole.getName());
            throw new ApiConflictException(String.format("Role %s already exists", appRole.getName()));
        });

        log.info("Creating role: {}", appRole);
        appRoleRepository.save(appRole);
    }

    public void addRoleToUser(String username, Role roleName) {
        AppUser appUser = appUserService.findByUsername(username);
        AppRole appRole = appRoleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Role not found: {}", roleName);
                    return new ApiNotFoundException(String.format("Role %s not found", roleName));
                });

        if (appUser.getAppUserRoles().contains(appRole)) {
            log.error("User {} already has role {}", username, roleName);
            throw new ApiConflictException(String.format("User %s already has role %s", username, roleName));
        }

        log.info("Adding role {} to user {}", roleName, username);
        appUser.getAppUserRoles().add(appRole);
    }
}
