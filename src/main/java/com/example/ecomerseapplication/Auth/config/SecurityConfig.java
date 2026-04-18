package com.example.ecomerseapplication.Auth.config;

import com.example.ecomerseapplication.Auth.helpers.EndpointMatcher;
import  org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, EndpointMatcher endpointMatcher) throws Exception {
        http
                // Disable CSRF for simplicity (useful for APIs)
                .csrf(AbstractHttpConfigurer::disable)
                // Allow CORS if needed
                .cors(Customizer.withDefaults())
                // Define access rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                request -> endpointMatcher.isPublicOrSemiProtected(request.getRequestURI())
                        ).permitAll() // public endpoints
                        .anyRequest().authenticated() // everything else needs auth
                )
                // Tell Spring this is a resource server using JWT tokens (from Keycloak)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess != null && realmAccess.containsKey("roles")) {

                Collection<String> roles = safeStringCollection(realmAccess.get("roles"));

                authorities.addAll(
                        roles.stream()
                                .map(role -> "ROLE_" + role.toUpperCase())
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                );

            }

            return authorities;
        });

        return converter;
    }

    private Collection<String> safeStringCollection(Object obj) {
        if (obj instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }

}
