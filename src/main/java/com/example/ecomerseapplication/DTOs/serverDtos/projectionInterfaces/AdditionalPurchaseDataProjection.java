package com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces;

import java.time.Instant;

public interface AdditionalPurchaseDataProjection {
    String getPaymentMethod();
    int getProductTotal();
    String getRecipientName();
    String getRecipientPhone();
    Instant getDeliveryDate();
}
