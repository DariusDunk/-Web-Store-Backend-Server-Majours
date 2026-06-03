package com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces;

import java.time.Instant;

public interface PurchaseProjection {
    Long getId();
    String getPurchaseCode();
    String getRecipientName();
    String getRecipientPhone();
    String getPaymentMethod();
    String getDeliveryAddress();
    String getEmail();
    Instant getPurchaseDate();
    Instant getDeliveryDate();
    Integer getTotalCost();

    String getUserId();
    String getUserName();
    String getUserFamilyName();

    String getDeliveryStatus();
}
