package com.example.ecomerseapplication.Auth.config;

import  org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for simplicity (useful for APIs)
                .csrf(AbstractHttpConfigurer::disable)
                // Allow CORS if needed
                .cors(Customizer.withDefaults())
                // Define access rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/customer/login/customer",
                                "/customer/register/customer",
                                "/public/**",
                                "/docs/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/attributes/**",
//                                "/**/**",
                                "**"
                        ).permitAll() // public endpoints
                        .anyRequest().authenticated() // everything else needs auth
                )
                // Tell Spring this is a resource server using JWT tokens (from Keycloak)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
