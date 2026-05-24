package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductForSaleUpdateRequest(
        @JsonProperty("name")
        String name,
        @JsonProperty("product_code")
        String productCode,
        @JsonProperty("explicit_discount")
        Short explicitDiscount
) {
}
