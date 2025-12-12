package com.example.ecomerseapplication.DTOs.responses;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CompactProductQuantityPairResponse {
    public CompactProductResponse compactProductResponse;
    public short quantity;
}
