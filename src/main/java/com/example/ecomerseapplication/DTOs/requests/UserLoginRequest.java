package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(
        @NotBlank
        @JsonProperty("identifier")
        String identifier,
        @NotBlank
        @JsonProperty("password")
        String password,
        @NotNull
        @JsonProperty("remember_me")
        Boolean rememberMe,
        @JsonProperty("client_type")
        String clientType
        ) {
}
