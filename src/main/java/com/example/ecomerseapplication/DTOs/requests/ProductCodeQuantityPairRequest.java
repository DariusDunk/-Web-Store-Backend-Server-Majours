package com.example.ecomerseapplication.DTOs.requests;

public record ProductCodeQuantityPairRequest(
        String productCode,
        short quantity
) {
}
