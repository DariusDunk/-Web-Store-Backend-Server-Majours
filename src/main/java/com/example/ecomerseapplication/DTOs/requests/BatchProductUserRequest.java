package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BatchProductUserRequest(
        @JsonProperty("customer_id")
        Long customerId,
        @JsonProperty("product_codes")
        List<String> productCodes) {
}
