package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public class ReviewRequest {
    @JsonProperty("user_id")
    public long customerId;
    @JsonProperty("product_code")
    public String productCode;
    @JsonProperty("review_text")
    public String reviewText;
    public short rating;
}
