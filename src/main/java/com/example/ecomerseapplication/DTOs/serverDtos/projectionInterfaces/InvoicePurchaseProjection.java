package com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces;

import com.example.ecomerseapplication.enums.DeliveryStatus;
import com.example.ecomerseapplication.enums.PaymentMethod;

import java.time.Instant;

public interface InvoicePurchaseProjection {

    String getPurchaseCode();
    Instant getDate();
    int getTotalCost();
    int getShippingFee();
    int getProductTotal();
    DeliveryStatus getDeliveryStatus();
    PaymentMethod getPaymentMethod();
    String getContactName();
    String getAddress();
    String getEmail();

}
