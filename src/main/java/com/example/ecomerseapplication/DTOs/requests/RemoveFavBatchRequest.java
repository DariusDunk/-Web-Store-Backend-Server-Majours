package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RemoveFavBatchRequest(
        @NotNull
        @JsonProperty("current_page")
        short currentPage,
        @NotEmpty
        @JsonProperty("product_codes")
        List<String> productCodes) {
}
