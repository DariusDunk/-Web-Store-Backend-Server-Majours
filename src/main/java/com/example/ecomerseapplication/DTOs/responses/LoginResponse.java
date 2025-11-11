package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        @JsonProperty("username")
        String username,
        @JsonProperty("role")
        String role,
        @JsonProperty("TokenResponse")
        TokenResponse tokenResponse
) {
}
