package com.example.spring.model.domain;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * @author Yogi
 * @since 07/02/2022
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AppRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    private Role name;
    @UniqueElements
    @ToString.Exclude
    @ManyToMany(
            mappedBy = "appUserRoles",
            fetch = FetchType.LAZY
    )
    private List<AppUser> appUser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AppRole appRole = (AppRole) o;
        return id != null && Objects.equals(id, appRole.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
