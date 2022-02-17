package com.example.spring.configuration;

import com.example.spring.exception.CustomAccessDeniedHandler;
import com.example.spring.filter.CustomAuthenticationFilter;
import com.example.spring.filter.CustomAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final String ADMIN = "ROLE_ADMIN";
    private final String USER = "ROLE_USER";
    private final String[] ALLOWED_PATHS = {
            "/api/v1/login",
            "/api/v1/registration/**",
            "/api/v1/session/**",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/v*/api-docs"
    };
    private final String[] SECURED_ADMIN_PATHS = {
            "/api/v1/user/**",
            "/api/v1/role/**"
    };
    private final String[] SECURED_USER_ADMIN_PATHS = {
    };

    @Autowired
    public WebSecurityConfiguration(@Lazy UserDetailsService userDetailsService, CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthorizationFilter customAuthorizationFilter = new CustomAuthorizationFilter();
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean());
        customAuthenticationFilter.setFilterProcessesUrl("/api/v1/login");

        http.cors().and().csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers(ALLOWED_PATHS).permitAll();
        http.authorizeRequests().antMatchers(SECURED_ADMIN_PATHS).hasAuthority(ADMIN);
        http.authorizeRequests().antMatchers(SECURED_USER_ADMIN_PATHS).hasAnyAuthority(USER, ADMIN);
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling().accessDeniedHandler(customAccessDeniedHandler);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
