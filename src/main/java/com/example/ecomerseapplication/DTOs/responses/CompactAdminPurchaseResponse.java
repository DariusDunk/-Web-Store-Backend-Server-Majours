package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record CompactAdminPurchaseResponse(
        Long id,
        @JsonProperty("purchase_code")
        String purchaseCode,
        @JsonProperty("purchase_date")
        Instant purchaseDate,
        @JsonProperty("recipient_detail")
        RecipientResponse recipientDetail,
        @JsonProperty("use_id")
        String userId,
        @JsonProperty("user_name")
        String userName,
        @JsonProperty("total_cost_cents")
        Integer totalCostCents,
        @JsonProperty("delivery_status")
        String deliveryStatus,
        @JsonProperty("allowed_actions")
        List<String> allowedActions,
        @JsonProperty("email")
        String email
        ) {
}
