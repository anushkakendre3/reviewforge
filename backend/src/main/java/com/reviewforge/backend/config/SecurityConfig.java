package com.reviewforge.backend.config;

import com.reviewforge.backend.service.JwtAuthFilter;
import com.reviewforge.backend.service.JwtService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService) {
        JwtAuthFilter filter = new JwtAuthFilter();
        filter.setJwtService(jwtService);
        return filter;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter(
            JwtAuthFilter jwtAuthFilter) {

        FilterRegistrationBean<JwtAuthFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(jwtAuthFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);

        return registration;
    }
}