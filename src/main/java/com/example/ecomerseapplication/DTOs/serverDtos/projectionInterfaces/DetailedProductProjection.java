package com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces;

public interface DetailedProductProjection {
    Integer getId();
    String getProductCode();
    String getName();
    Integer getOriginalPriceStotinki();
    Short getDefaultSaleDiscount();
    Short getExplicitDiscount();
    Short getRating();
    Integer getReviewCount();
    String getImageUrl();
    Boolean getIsInStock();
    String getCategoryName();
    String getManufacturerName();
    Integer getQuantityInStock();
    Integer getManufacturerId();
    Integer getCategoryId();
}
