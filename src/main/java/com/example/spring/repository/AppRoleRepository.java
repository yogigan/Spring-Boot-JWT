package com.example.spring.repository;

import com.example.spring.model.domain.AppRole;
import com.example.spring.model.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Yogi
 * @since 07/02/2022
 */
@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByName(Role roleName);

}
