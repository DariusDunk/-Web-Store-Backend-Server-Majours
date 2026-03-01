package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(
        @NotNull
        @JsonProperty("identifier")
        String identifier,
        @NotNull
        @JsonProperty("password")
        String password
        ) {
}
