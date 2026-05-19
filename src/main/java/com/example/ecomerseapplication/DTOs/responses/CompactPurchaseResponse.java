package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;


public record CompactPurchaseResponse(
        @JsonProperty("purchase_code")
         String purchaseCode,
         @JsonProperty("purchase_date")
         Instant purchaseDate,
         @JsonProperty("total_cost")
         int totalCost,
         @JsonProperty("shipping_fee")
         int shippingFee,
         @JsonProperty("status")
         String status,
         @JsonProperty("delivery_address")
         String deliveryAddress,
         @JsonProperty("products")
         List<ProductForCompactPurchaseHistoryResponse> compactProducts
) {



}
