package com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces;

public interface CompactProductProjection {
    String getProductCode();
    String getName();
    int getOriginalPriceStotinki();
    int getDiscountedPriceStotinki();
//    Short getDefaultSaleDiscount();
//    Short getExplicitDiscount();
    short getRating();
    int getReviewCount();
    String getImageUrl();
    boolean getIsInStock();
}
