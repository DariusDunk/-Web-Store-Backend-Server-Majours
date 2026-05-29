package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductImageResponse(
        @JsonProperty("file_name")
        String fileName,
        @JsonProperty("product_code")
        String productCode
        ) {
}
