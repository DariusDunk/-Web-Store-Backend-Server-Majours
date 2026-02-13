package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RemoveOneFavRequest(
        @JsonProperty("current_page")
        short currentPage,
        @JsonProperty("product_code")
        String productCode) {
}
