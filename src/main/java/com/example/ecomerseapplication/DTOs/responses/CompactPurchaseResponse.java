package com.example.ecomerseapplication.DTOs.responses;

import com.example.ecomerseapplication.DTOs.CompactProductQuantityPairResponse;

import java.time.LocalDateTime;
import java.util.List;

public class CompactPurchaseResponse {
    public String purchaseCode;
    public LocalDateTime purchaseDate;
    public int totalCost;

    public List<CompactProductQuantityPairResponse> compactProductQuantityPairs;


}
