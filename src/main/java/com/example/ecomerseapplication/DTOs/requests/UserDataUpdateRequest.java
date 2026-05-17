package com.example.ecomerseapplication.DTOs.requests;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UserDataUpdateRequest(
        @NotBlank
        @JsonProperty("first_name")
        String firstName,
        @NotBlank
        @JsonProperty("last_name")
        String familyName,
        @NotBlank
        @JsonProperty("phone_number")
        String phoneNumber) {
}
