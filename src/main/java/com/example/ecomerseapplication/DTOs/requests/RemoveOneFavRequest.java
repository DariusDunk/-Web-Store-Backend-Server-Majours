package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record RemoveOneFavRequest(
        @NotNull
        @JsonProperty("current_page")
        short currentPage,
        @NotNull
        @JsonProperty("product_code")
        String productCode) {
}
