package com.example.ecomerseapplication.DTOs.requests;

import jakarta.validation.constraints.NotBlank;

public class SavedRecipientDetailsRequest {
    @NotBlank
    public String contactName;
    @NotBlank
    public String contactNumber;
    @NotBlank
    public String address;
}
