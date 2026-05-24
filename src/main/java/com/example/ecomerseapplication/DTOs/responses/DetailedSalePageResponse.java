package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record DetailedSalePageResponse(
        Long id,
        @JsonProperty("name")
        String name,
        @JsonProperty("default_discount")
        Short defaultDiscount,
        @JsonProperty("start_date")
        Instant startDate,
        @JsonProperty("end_date")
        Instant endDate,
        @JsonProperty("is_active")
        Boolean isActive,
        @JsonProperty("product_count")
        Integer productCount
) {
}
