package com.example.ecomerseapplication.DTOs.responses;

import java.time.Instant;
import java.util.List;

public class CompactPurchaseResponse {
    public String purchaseCode;
    public Instant purchaseDate;
    public int totalCost;

    public List<CompactProductQuantityPairResponse> compactProductQuantityPairs;


}
