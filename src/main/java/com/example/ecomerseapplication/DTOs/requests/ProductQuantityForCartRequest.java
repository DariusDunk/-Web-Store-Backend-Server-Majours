package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductQuantityForCartRequest(@JsonProperty("product_code")
                                            @NotBlank
                                            String productCode,
                                            @Positive
                                            @JsonProperty("quantity")
                                            short quantity) {


}
