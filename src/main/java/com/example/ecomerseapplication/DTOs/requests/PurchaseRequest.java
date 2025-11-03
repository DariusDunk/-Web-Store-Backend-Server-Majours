package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseRequest {
    public long customerId;
    @JsonProperty("recipient_request")
    public SavedRecipientDetailsRequest savedRecipientDetailsRequest;

}
