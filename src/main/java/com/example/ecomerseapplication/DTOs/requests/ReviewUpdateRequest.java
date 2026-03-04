package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;

@ToString
public class ReviewUpdateRequest {
    @NotBlank
    @JsonProperty("product_code")
    public String productCode;
    @NotNull
    @JsonProperty("review_text")
    public String reviewText;
    @NotNull
    public short rating;
}
