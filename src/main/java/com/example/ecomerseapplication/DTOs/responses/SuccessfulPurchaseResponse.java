package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SuccessfulPurchaseResponse(
        @JsonProperty("purchase_code")
        String purchaseCode,
        @JsonProperty("status")
        String status,
        @JsonProperty("total_cost")
        Integer totalCost,
        @JsonProperty("shipping_fee")
        Integer shippingFee,
        @JsonProperty("purchase_method")
        String purchaseMethod
) {
}
