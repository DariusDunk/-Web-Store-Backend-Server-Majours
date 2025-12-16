package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CustomerResponse(
        @JsonProperty("customer_id")
        Long customerId,
        @JsonProperty("username")
        String username,
        @JsonProperty("customer_pfp")
        String customerPfp,
        @JsonProperty("role")
        String role,
        Long userId
        ) {

}
