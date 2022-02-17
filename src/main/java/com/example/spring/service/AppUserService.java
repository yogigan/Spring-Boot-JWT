package com.example.spring.service;

import com.example.spring.exception.ApiConflictException;
import com.example.spring.exception.ApiNotFoundException;
import com.example.spring.model.domain.AppRole;
import com.example.spring.model.domain.AppUser;
import com.example.spring.model.domain.ConfirmationToken;
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
    private static final String EMAIL_ALREADY_TAKEN_MESSAGE = "Email %s is already taken";
    private static final String USER_NOT_FOUND_MESSAGE = "User with username or email %s not found";
    private static final String EMAIL_SUBJECT = "Confirmation Account";

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // check if user exists
        Optional<AppUser> appUser = appUserRepository.findByEmail(email);
        if (!appUser.isPresent()) {
            appUser = appUserRepository.findByUsername(email);
            if (!appUser.isPresent()) {
                throw new AuthenticationCredentialsNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, email));
            }
        }
        return appUser.get();
    }

    public String registerUser(AppUser appUser) throws NonUniqueResultException {
        // check if email is already taken
        appUserRepository.findByEmail(appUser.getEmail()).ifPresent(user -> {
            throw new ApiConflictException(String.format(EMAIL_ALREADY_TAKEN_MESSAGE, appUser.getEmail()));
        });

        //check if username is already taken
        appUserRepository.findByUsername(appUser.getUsername()).ifPresent(user -> {
            throw new ApiConflictException(String.format(EMAIL_ALREADY_TAKEN_MESSAGE, appUser.getUsername()));
        });

        // save user with encrypted password
        Optional<AppRole> appRole = appRoleRepository.findByName("ROLE_USER");
        List<AppRole> roles = appRole.map(Arrays::asList).orElseGet(Arrays::asList);
        appUser.setRoles(roles);
        appUser.setPassword(bCryptPasswordEncoder.encode(appUser.getPassword()));
        appUserRepository.save(appUser);

        // save confirmation token
        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .token(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .appUser(appUser)
                .build();
        confirmationTokenRepository.save(confirmationToken);

        // send email confirmation
        if (IS_USE_EMAIL_VERIFICATION) {
            mailService.sendEmail(appUser.getEmail(), EMAIL_SUBJECT,
                    buildEmail(appUser.getFirstName() + " " + appUser.getLastName(),
                            BASE_URL + "/api/v1/registration?token=" + confirmationToken.getToken()));
        }
        return confirmationToken.getToken();
    }

    public void setEnabled(Long id) {
        appUserRepository.findById(id).ifPresent(appUser -> {
            appUser.setIsEnabled(true);
            appUserRepository.save(appUser);
        });
    }

    public AppUser saveUser(AppUser appUser) {
        log.info("Creating user: {}", appUser);
        return appUserRepository.save(appUser);
    }

    public AppRole saveRole(AppRole appRole) {
        log.info("Creating role: {}", appRole);
        return appRoleRepository.save(appRole);
    }

    public void addRoleToUser(String username, String roleName) {
        log.info("Adding role {} to user {}", roleName, username);
        AppUser appUser = findByUsername(username);
        AppRole appRole = appRoleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Role not found: {}", roleName);
                    return new ApiNotFoundException("Role not found");
                });
        appUser.getRoles().add(appRole);
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
        log.info("Finding user by username: {}", username);
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new ApiNotFoundException("User not found");
                });
        return UserInfoResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(AppRole::getName).collect(Collectors.toList()))
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
