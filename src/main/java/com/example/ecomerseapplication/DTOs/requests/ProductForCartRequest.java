package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;

@ToString
public class ProductForCartRequest {
    @NotBlank
    @JsonProperty("product_code")
    public String productCode;
    @NotNull
    @JsonProperty("do_increment")
    public Boolean doIncrement;
}
