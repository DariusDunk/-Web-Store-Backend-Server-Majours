package com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces;

public interface ProductForDetailedPurchaseProjection {
    String getProductCode();
    String getProductName();
    Integer getSinglePrice();
    Short getRating();
    Integer getReviewCount();
    String getImageUrl();
    Integer getQuantity();
}
