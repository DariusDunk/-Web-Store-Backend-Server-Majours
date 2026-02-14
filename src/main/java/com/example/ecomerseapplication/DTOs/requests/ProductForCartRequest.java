package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public class ProductForCartRequest {
    @JsonProperty("product_code")
    public String productCode;
    @JsonProperty("do_increment")
    public Boolean doIncrement;
}
