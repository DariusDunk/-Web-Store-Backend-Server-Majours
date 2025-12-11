package com.example.ecomerseapplication.DTOs.responses;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CompactProductResponse {
    public String productCode;
    public String name;
    public int originalPriceStotinki;
    public int salePriceStotinki;
    public short rating;
    public int reviewCount;
    public String imageUrl;
    public boolean inFavourites = false;
//TODO boolean za nali4nost
}
