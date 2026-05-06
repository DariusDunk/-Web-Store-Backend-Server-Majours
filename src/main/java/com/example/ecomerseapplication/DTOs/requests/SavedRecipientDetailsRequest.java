package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class SavedRecipientDetailsRequest {
    @NotBlank
    @JsonProperty("contact_name")
    public String contactName;
    @NotBlank
    @JsonProperty("contact_number")
    public String contactNumber;
    @NotBlank
    @JsonProperty("address")
    public String address;
}
