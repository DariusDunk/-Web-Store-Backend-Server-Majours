package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateSessionRequest(
        @JsonProperty("session_id")
        String sessionId,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("customer_id")
        String customerId//TODO ako trqbva dobavi o6te ne6ta
        ) {
}
