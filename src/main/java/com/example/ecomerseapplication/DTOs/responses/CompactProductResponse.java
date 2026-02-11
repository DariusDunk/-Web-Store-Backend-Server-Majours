package com.example.ecomerseapplication.DTOs.responses;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CompactProductResponse {
    public String productCode;
    public String name;
    public int originalPriceStotinki;
    public int salePriceStotinki;
    public short rating;
    public int reviewCount;
    public String imageUrl;
    public boolean isInStock = false;
}
