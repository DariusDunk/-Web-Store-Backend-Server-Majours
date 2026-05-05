package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RecipientDataRequest(
        @NotBlank
        @JsonProperty("contact_name")
        String contactName,
        @JsonProperty("contact_number")
        @NotBlank
        String contactNumber,
        @JsonProperty("address")
        @NotBlank
        String address
) {
}
