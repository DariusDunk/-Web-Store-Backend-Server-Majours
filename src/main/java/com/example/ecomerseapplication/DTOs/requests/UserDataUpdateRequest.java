package com.example.ecomerseapplication.DTOs.requests;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserDataUpdateRequest(
        @NotBlank
        @Pattern(
                regexp = "^[\\p{L}-]+$",
                message = "Името може да съдържа само букви и тире"
        )
        @JsonProperty("first_name")
        String firstName,
        @NotBlank
        @Pattern(
                regexp = "^[\\p{L}-]+$",
                message = "Фамилията може да съдържа само букви и тире"
        )
        @JsonProperty("last_name")
        String familyName,
        @NotBlank
        @JsonProperty("phone_number")
        String phoneNumber) {
}
