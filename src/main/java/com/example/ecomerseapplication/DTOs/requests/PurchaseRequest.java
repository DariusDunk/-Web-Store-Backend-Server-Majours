package com.example.ecomerseapplication.DTOs.requests;

import com.example.ecomerseapplication.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PurchaseRequest(
        @JsonProperty("products")
        List<ProductQuantityForCartRequest> products,
        @JsonProperty("is_direct_purchase")
        boolean isDirectPurchase,
        @JsonProperty("recipientData")
        RecipientDataRequest recipientData,
        @JsonProperty("payment_method")
        PaymentMethod paymentMethod,
        @JsonProperty("email")
        String email
) {

}
