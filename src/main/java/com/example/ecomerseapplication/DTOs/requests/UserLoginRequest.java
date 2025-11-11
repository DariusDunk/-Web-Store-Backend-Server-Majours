package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserLoginRequest(
        @JsonProperty("identifier")
        String identifier,
        @JsonProperty("password")
        String password
        ) {
}
