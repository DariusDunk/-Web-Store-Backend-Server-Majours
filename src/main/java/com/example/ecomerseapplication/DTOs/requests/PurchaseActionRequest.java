package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PurchaseActionRequest(
        @JsonProperty("id")
        Long purchaseId,
        @JsonProperty("action")
        String action
        ) {
}
