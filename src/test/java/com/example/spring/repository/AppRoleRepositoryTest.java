package com.example.spring.repository;

import com.example.spring.model.domain.AppRole;
import com.example.spring.model.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static com.example.spring.model.domain.Role.ROLE_USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class AppRoleRepositoryTest {

    @Autowired
    private AppRoleRepository appRoleRepository;

    @BeforeEach
    void setUp() {
        appRoleRepository.deleteAll();
    }

    @Test
    void testFindByNameExists() {
        //given
        Role name = ROLE_USER;
        AppRole appRole = AppRole.builder()
                .name(name)
                .build();
        appRoleRepository.save(appRole);

        //when
        Optional<AppRole> expected = appRoleRepository.findByName(name);


        //then
        assertThat(expected.isPresent()).isTrue();
        assertThat(expected).isNotEmpty();
        assertThat(expected.get().getName()).isEqualTo(name);
    }

    @Test
    void testFindByNameNotExists() {
        //when
        Optional<AppRole> expected = appRoleRepository.findByName(ROLE_USER);

        //then
        assertThat(expected.isPresent()).isFalse();
        assertThat(expected).isEmpty();
    }
}