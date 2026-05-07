package com.example.ecomerseapplication.DTOs.responses;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CompactProductQuantityPairResponse {
    public CompactProductResponse compactProductResponse;
    public short quantity;
}
