package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.KeycloakTokenResponse;
import com.example.ecomerseapplication.DTOs.responses.LoginResponse;
import com.example.ecomerseapplication.Entities.Session;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class LoginResponseMapper {
    public static LoginResponse fromKeycloakResponseAndSession(KeycloakTokenResponse keycloakTokenResponse, Session session) {
        return new LoginResponse(keycloakTokenResponse.accessToken(),
                keycloakTokenResponse.expiresIn(),
                keycloakTokenResponse.refreshExpiresIn(),
                keycloakTokenResponse.refreshToken(),
                session.getSessionId(),
                ChronoUnit.SECONDS.between(Instant.now(), session.getExpiresAt()));
    }
}
