package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
        @NotNull
        @JsonProperty("refresh_token")
        String refreshToken
        ) {
}
