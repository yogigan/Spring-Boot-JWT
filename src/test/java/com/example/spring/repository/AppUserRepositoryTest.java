package com.example.spring.repository;

import com.example.spring.model.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
    }

    @Test
    void testFindByEmailExists() {
        //given
        String email = "mail@gmail.com";
        AppUser appUser = AppUser.builder()
                .firstName("Yogi")
                .lastName("Pratama")
                .email(email)
                .username("yogi")
                .password("toor")
                .build();
        appUserRepository.save(appUser);

        //when
        Optional<AppUser> expected = appUserRepository.findByEmail(email);

        //then
        assertThat(expected.isPresent()).isTrue();
        assertThat(expected).isNotEmpty();
        assertThat(expected.get().getEmail()).isEqualTo(email);

    }

    @Test
    void testFindByEmailNotExists() {
        //given
        String email = "mail@gmail.com";

        //when
        Optional<AppUser> expected = appUserRepository.findByEmail(email);

        //then
        assertThat(expected.isPresent()).isFalse();
        assertThat(expected).isEmpty();
    }

    @Test
    void testFindByUsernameExists() {
        //given
        String username = "yogi";
        AppUser appUser = AppUser.builder()
                .firstName("Yogi")
                .lastName("Pratama")
                .email("mail@gmail.com")
                .username(username)
                .password("toor")
                .build();
        appUserRepository.save(appUser);

        //when
        Optional<AppUser> expected = appUserRepository.findByUsername(username);

        //then
        assertThat(expected.isPresent()).isTrue();
        assertThat(expected).isNotEmpty();
        assertThat(expected.get().getUsername()).isEqualTo(username);
    }

    @Test
    void testFindByUsernameNotExists() {
        //given
        String username = "yogi";

        //when
        Optional<AppUser> expected = appUserRepository.findByUsername(username);

        //then
        assertThat(expected.isPresent()).isFalse();
        assertThat(expected).isEmpty();
    }
}