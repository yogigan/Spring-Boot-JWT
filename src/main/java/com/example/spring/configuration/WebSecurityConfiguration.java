package com.example.spring.configuration;

import com.example.spring.exception.CustomAccessDeniedHandler;
import com.example.spring.filter.CustomAuthenticationFilter;
import com.example.spring.filter.CustomAuthorizationFilter;
import com.example.spring.service.AppUserService;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private AppUserService userDetailsService;
    private CustomAuthenticationFilter customAuthenticationFilter;
    private final CustomAuthorizationFilter customAuthorizationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private static final String[] ALLOWED_PATHS = {
            "/api/v1/login",
            "/api/v1/registration/**",
            "/api/v1/session/**",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/v*/api-docs"
    };
    private static final String[] SECURED_ADMIN_PATHS = {
            "/api/v1/user/**",
            "/api/v1/role/**"
    };

    @Autowired
    public void setUserDetailsService(@Lazy AppUserService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setCustomAuthenticationFilter(@Lazy CustomAuthenticationFilter customAuthenticationFilter) {
        this.customAuthenticationFilter = customAuthenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        customAuthenticationFilter.setAuthenticationManager(authenticationManager());
        customAuthenticationFilter.setFilterProcessesUrl("/api/v1/login");

        http.cors().and().csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers(ALLOWED_PATHS).permitAll();
        http.authorizeRequests().antMatchers(SECURED_ADMIN_PATHS).hasAuthority("ROLE_ADMIN");
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling().accessDeniedHandler(customAccessDeniedHandler);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
