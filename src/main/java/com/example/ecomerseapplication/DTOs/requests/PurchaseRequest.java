package com.example.ecomerseapplication.DTOs.requests;

import com.example.ecomerseapplication.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PurchaseRequest(
        @JsonProperty("products")
        @NotEmpty
        List<ProductQuantityForCartRequest> products,
        @JsonProperty("is_direct_purchase")
        @NotNull
        Boolean isDirectPurchase,
        @JsonProperty("recipientData")
        RecipientDataRequest recipientData,
        @JsonProperty("payment_method")
        PaymentMethod paymentMethod,
        @JsonProperty("email")
        String email
) {

}
