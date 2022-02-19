package com.example.spring.configuration;

import com.example.spring.model.domain.AppRole;
import com.example.spring.model.domain.AppUser;
import com.example.spring.service.AppUserService;
import com.github.javafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yogi
 * @since 06/02/2022
 */
@Configuration
public class DataConfiguration {

    @Bean
    @Profile("dev")
    public CommandLineRunner commandLineRunner(Faker faker, AppUserService userService, BCryptPasswordEncoder bcryptPasswordEncoder) {
        return arg -> {
            List<AppUser> users = new ArrayList<>();
            String[] roles = {"ROLE_ADMIN", "ROLE_USER"};
            for (String role : roles) {
                userService.saveRole(
                        AppRole.builder()
                                .name(role)
                                .build()
                );
            }
            for (int i = 0; i < 10; i++) {

                // Create user
                users.add(AppUser.builder()
                        .firstName(faker.name().firstName())
                        .lastName(faker.name().lastName())
                        .email(faker.internet().emailAddress())
                        .password(bcryptPasswordEncoder.encode(faker.internet().password()))
                        .username(faker.name().username())
                        .build()
                );

            }
            saveAdmin(userService, bcryptPasswordEncoder);
            userService.saveAll(users);

            // Add role to user
            for (AppUser user : users) {
                userService.addRoleToUser(
                        user.getUsername(),
                        roles[faker.random().nextInt(0, (roles.length - 1))]
                );
            }
        };
    }

    public void saveAdmin(AppUserService userService, BCryptPasswordEncoder bcryptPasswordEncoder) {
        userService.saveUser(
                AppUser.builder()
                        .username("admin")
                        .password(bcryptPasswordEncoder.encode("toor"))
                        .firstName("Admin")
                        .lastName("Admin")
                        .email("bachtiarnuryogipratama1@gmail.com")
                        .isEnabled(true)
                        .isLocked(false)
                        .build()
        );
        userService.addRoleToUser("admin", "ROLE_ADMIN");
    }
}
