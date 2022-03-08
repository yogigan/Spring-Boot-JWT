package com.example.spring.service;

import com.example.spring.exception.ApiConflictException;
import com.example.spring.exception.ApiNotFoundException;
import com.example.spring.model.domain.AppRole;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.ConfirmationToken;
import com.example.spring.model.domain.Role;
import com.example.spring.model.response.UserInfoResponse;
import com.example.spring.repository.AppRoleRepository;
import com.example.spring.repository.AppUserRepository;
import com.example.spring.repository.ConfirmationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NonUniqueResultException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.spring.model.domain.Role.ROLE_ADMIN;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AppUserService implements UserDetailsService {

    @Value("${parameter.value.base-url}")
    private String BASE_URL;
    @Value("${parameter.value.is-use-email-verification}")
    private boolean IS_USE_EMAIL_VERIFICATION;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AppUserRepository appUserRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final AppRoleRepository appRoleRepository;
    private final MailService mailService;
    private static final String EMAIL_ALREADY_TAKEN_MESSAGE = "Email %s is already exist";
    private static final String USERNAME_ALREADY_TAKEN_MESSAGE = "Username %s is already exist";
    private static final String USER_NOT_FOUND_MESSAGE = "User with username or email %s not found";
    private static final String EMAIL_SUBJECT = "Confirmation Account";

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // check if user exists by email
        Optional<AppUser> appUser = appUserRepository.findByEmail(email);
        if (!appUser.isPresent()) {
            // check if user exists by username
            appUser = appUserRepository.findByUsername(email);
            if (!appUser.isPresent()) {
                throw new AuthenticationCredentialsNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, email));
            }
        }
        log.error("User with username or email {} not found", email);
        return appUser.get();
    }

    public String registerUser(AppUser appUser) throws NonUniqueResultException {

        // save user
        saveUser(appUser);

        // save confirmation token
        log.info("Save confirmation token for user {}", appUser.getUsername());
        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .appUser(appUser)
                .build();
        confirmationTokenRepository.save(confirmationToken);

        // send email confirmation
        if (IS_USE_EMAIL_VERIFICATION) {
            log.info("Send email confirmation to {}", appUser.getEmail());
            mailService.sendEmail(appUser.getEmail(), EMAIL_SUBJECT,
                    buildEmail(appUser.getFirstName() + " " + appUser.getLastName(),
                            BASE_URL + "/api/v1/registration?token=" + confirmationToken.getToken()));
        }
        return confirmationToken.getToken();
    }

    public void setUserEnable(Long id) {
        AppUser appUser = findById(id);
        appUser.setIsEnabled(true);
        log.info("Set user {} enable", appUser.getUsername());
        appUserRepository.save(appUser);
    }

    public AppUser findById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", id);
                    return new ApiNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, id));
                });
    }

    public AppUser saveUser(AppUser appUser) {
        // check if email is already exist
        appUserRepository.findByEmail(appUser.getEmail()).ifPresent(user -> {
            log.error(String.format(EMAIL_ALREADY_TAKEN_MESSAGE, appUser.getEmail()));
            throw new ApiConflictException(String.format(EMAIL_ALREADY_TAKEN_MESSAGE, appUser.getEmail()));
        });

        //check if username is already exist
        appUserRepository.findByUsername(appUser.getUsername()).ifPresent(user -> {
            log.error(String.format(USERNAME_ALREADY_TAKEN_MESSAGE, appUser.getUsername()));
            throw new ApiConflictException(String.format(USERNAME_ALREADY_TAKEN_MESSAGE, appUser.getUsername()));
        });

        // save user with encrypted password
        log.info("Saving user with encrypted password");
        Optional<AppRole> appRole = appRoleRepository.findByName(ROLE_ADMIN);
        List<AppRole> roles = appRole.map(Arrays::asList).orElseGet(Arrays::asList);
        appUser.setAppUserRoles(roles);
        appUser.setPassword(bCryptPasswordEncoder.encode(appUser.getPassword()));
        return appUserRepository.save(appUser);
    }

    public AppRole saveRole(AppRole appRole) {
        appRoleRepository.findByName(appRole.getName()).ifPresent(role -> {
            log.error("Role with name {} already exist", appRole.getName());
            throw new ApiConflictException(String.format("Role %s already exists", appRole.getName()));
        });
        log.info("Creating role: {}", appRole);
        return appRoleRepository.save(appRole);
    }

    public List<AppRole> addRoleToUser(String username, Role roleName) {
        AppUser appUser = findByUsername(username);
        AppRole appRole = appRoleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Role not found: {}", roleName);
                    return new ApiNotFoundException("Role not found");
                });
        if (appUser.getAppUserRoles().contains(appRole)) {
            log.error("User {} already has role {}", username, roleName);
            throw new ApiConflictException(String.format("User %s already has role %s", username, roleName));
        }
        log.info("Adding role {} to user {}", roleName, username);
        appUser.getAppUserRoles().add(appRole);
        return appUser.getAppUserRoles();
    }

    public AppUser findByUsername(String username) {
        log.info("Finding user by username: {}", username);
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new ApiNotFoundException("User not found");
                });
    }

    public UserInfoResponse getUserInfoByUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new ApiNotFoundException("User not found");
                });
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
        return appUserRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    public void saveAll(List<AppUser> appUsers) {
        log.info("Saving all users");
        appUserRepository.saveAll(appUsers);
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
