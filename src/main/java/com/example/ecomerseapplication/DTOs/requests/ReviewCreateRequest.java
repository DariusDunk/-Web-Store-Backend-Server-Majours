package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReviewCreateRequest(
                                  @JsonProperty("product_code")
                                  String productCode,
                                  @JsonProperty("review_text")
                                  String reviewText,
                                  short rating) {
}
