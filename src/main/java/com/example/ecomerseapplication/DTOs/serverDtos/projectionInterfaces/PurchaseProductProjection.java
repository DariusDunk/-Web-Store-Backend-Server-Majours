package com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces;

public interface PurchaseProductProjection {
    String getProductCode();
    String getProductName();
    int getOriginalPriceStotinki();
    Short getDefaultSaleDiscount();
    Short getExplicitDiscount();
    short getRating();
    String getImageUrl();
    int getQuantity();

}
