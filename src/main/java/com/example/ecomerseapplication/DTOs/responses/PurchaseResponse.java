package com.example.ecomerseapplication.DTOs.responses;

import com.example.ecomerseapplication.DTOs.CompactProductQuantityPairResponse;
import com.example.ecomerseapplication.DTOs.SavedPurchaseDetailsResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseResponse {
    public SavedPurchaseDetailsResponse recipientDetailRequest;
    public SavedPurchaseDetailsResponse savedPurchaseDetailsResponse;
    public int totalCost;
    public LocalDateTime dateOfPurchase;
    public String purchaseCode;
    public List<CompactProductQuantityPairResponse> productQuantityPairs = new ArrayList<>();

}
