package com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces;

import com.example.ecomerseapplication.Entities.Product;

public interface PurchaseProductPairProjection {
    Long getPurchaseId();
    Product getPurchaseProduct();
}
