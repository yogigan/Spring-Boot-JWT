package com.example.spring.configuration;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * @author Yogi
 * @since 16/02/2022
 */
@Configuration
public class TimeZoneConfiguration {

    /**
     * Set the time zone to Asia/Jakarta
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"));
    }

}
