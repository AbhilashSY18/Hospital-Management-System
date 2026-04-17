package com.hospital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * DESIGN PRINCIPLE: Least Privilege (Security)
 * Each role is granted only the access it needs.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // FIX: permit static assets and H2 console without authentication
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/login", "/logout").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/beds/add", "/beds/delete/**").hasAnyRole("ADMIN")
                .requestMatchers("/beds/status/**").hasAnyRole("ADMIN", "NURSE")
                .requestMatchers("/patients/admit", "/patients/discharge/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/patients/transfer/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE")
                .requestMatchers("/resources/allocate/**", "/resources/release/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/reports/**").hasAnyRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            // FIX: Disable CSRF for H2 console AND allow frames from same origin
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            // FIX: Spring Boot 3.2 correct frameOptions API
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
