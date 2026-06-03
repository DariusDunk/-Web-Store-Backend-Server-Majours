package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActiveSessionsPDFRequest(
        @JsonProperty("total_count")
        String totalCount,
        @JsonProperty("auth_count")
        String authCount,
        @JsonProperty("guest_count")
        String guestCount,
        @JsonProperty("timestamp")
        String timestamp
        ) {
}
