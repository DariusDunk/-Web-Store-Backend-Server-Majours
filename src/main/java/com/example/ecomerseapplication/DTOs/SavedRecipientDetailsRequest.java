package com.example.ecomerseapplication.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SavedRecipientDetailsRequest(
//        @JsonProperty("contact_name")
        String contactName,
//        @JsonProperty("contact_number")
        String contactNumber,
        String address
) {
}
