package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("session_id")
        String sessionId,
        @JsonProperty("session_expires_in")
        Long sessionExpiresIn
//        , @JsonProperty("user_role")
//        String userRole
        ) {
}
