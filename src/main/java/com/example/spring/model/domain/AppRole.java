package com.example.spring.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Yogi
 * @since 07/02/2022
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
}
