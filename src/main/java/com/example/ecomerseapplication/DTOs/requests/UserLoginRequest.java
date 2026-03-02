package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank
        @JsonProperty("identifier")
        String identifier,
        @NotBlank
        @JsonProperty("password")
        String password
        ) {
}
