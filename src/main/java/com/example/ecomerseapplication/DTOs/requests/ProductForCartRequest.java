package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;

@ToString
public class ProductForCartRequest {
    @NotNull
    @JsonProperty("product_code")
    public String productCode;
    @NotNull
    @JsonProperty("do_increment")
    public Boolean doIncrement;
}
