package com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces;

import java.time.Instant;

public interface DetailedSaleProjection {
    String getName();
    Short getDefaultDiscount();
    Instant getStartDate();
    Instant getEndDate();
    Boolean getIsActive();
    Integer getProductCount();
}
