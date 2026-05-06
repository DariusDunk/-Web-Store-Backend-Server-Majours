package com.example.ecomerseapplication.DTOs.serverDtos;

public record TotalsDTO(
        int productTotal,
        int shippingFee,
        int totalCost
) {
}
