package com.example.ecomerseapplication.DTOs.responses;

import com.example.ecomerseapplication.DTOs.CompactProductQuantityPairResponse;
import com.example.ecomerseapplication.DTOs.RecipientDetailResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseResponse {
    public RecipientDetailResponse recipientDetailResponse;
    public int totalCost;
    public LocalDateTime dateOfPurchase;
    public String purchaseCode;
    public List<CompactProductQuantityPairResponse> productQuantityPairs = new ArrayList<>();

}
