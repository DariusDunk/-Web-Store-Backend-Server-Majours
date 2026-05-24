package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductOfSaleResponse(
        @JsonProperty("name")
        String name,
        @JsonProperty("product_code")
        String productCode,
        @JsonProperty("explicit_discount")
        Short explicitDiscount
        ) {
}
