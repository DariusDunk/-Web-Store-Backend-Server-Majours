package com.example.ecomerseapplication.DTOs.responses;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DetailedPurchaseAdditionalDataResponse(
        @JsonProperty("products_total")
        int productsTotal,
        @JsonProperty("recipient_name")
        String recipientName,
        @JsonProperty("recipient_phone")
        String recipientPhone,
        @JsonProperty("payment_method")
        String paymentMethod,
        @JsonProperty("products")
        List<DetailedPurchaseProductResponse> products
        ) {
}
