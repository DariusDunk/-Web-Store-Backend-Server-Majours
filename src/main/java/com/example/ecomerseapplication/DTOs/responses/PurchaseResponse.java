package com.example.ecomerseapplication.DTOs.responses;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PurchaseResponse {

    public SavedPurchaseDetailsResponse savedPurchaseDetailsResponse;
    public int totalCost;
    public Instant dateOfPurchase;
    public String purchaseCode;
    public List<CompactProductQuantityPairResponse> productQuantityPairs = new ArrayList<>();

}
