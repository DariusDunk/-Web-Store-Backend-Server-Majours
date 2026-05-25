package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record SaleCreateRequest(
        @NotBlank
        @JsonProperty("name")
        String name,
        @NotNull
        @JsonProperty("default_discount")
        Short defaultDiscount,
        @NotNull
        @JsonProperty("start_date")
        Instant startDate,
        @NotNull
        @JsonProperty("end_date")
        Instant endDate,
        @NotNull
        @JsonProperty("is_active")
        Boolean isActive,
        @JsonProperty("products")
        List<ProductForSaleUpdateRequest> products
) {
}
