package com.example.ecomerseapplication.DTOs.serverDtos;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.InvoicePurchaseProjection;

import java.util.List;

public record InvoiceFullDTO(
        InvoicePurchaseProjection invoicePurchaseProjection,
        List<CompactProductPricePairDTO> products
) {
}
