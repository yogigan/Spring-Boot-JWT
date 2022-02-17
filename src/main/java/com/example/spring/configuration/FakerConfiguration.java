package com.example.spring.configuration;

import com.github.javafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yogi
 * @since 06/02/2022
 */
@Configuration
public class FakerConfiguration {

    @Bean
    public Faker faker() {
        return new Faker();
    }
}
