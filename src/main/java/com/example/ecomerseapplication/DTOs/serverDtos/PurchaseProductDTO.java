package com.example.ecomerseapplication.DTOs.serverDtos;

import com.example.ecomerseapplication.Entities.Product;

public record PurchaseProductDTO(
        Product product,
        short quantity,
        int finalPrice
) {
}
