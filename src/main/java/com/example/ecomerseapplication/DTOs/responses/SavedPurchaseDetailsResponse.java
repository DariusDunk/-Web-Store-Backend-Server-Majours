package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SavedPurchaseDetailsResponse {
    @JsonProperty("contact_name")
    public String contactName;
    @JsonProperty("contact_number")
    public String contactNumber;
    @JsonProperty("address")
    public String address;

}
