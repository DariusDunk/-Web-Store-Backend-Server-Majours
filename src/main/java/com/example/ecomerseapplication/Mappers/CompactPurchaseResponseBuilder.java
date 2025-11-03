package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.CompactProductQuantityPairResponse;
import com.example.ecomerseapplication.DTOs.responses.CompactPurchaseResponse;
import com.example.ecomerseapplication.Entities.Purchase;
import java.util.List;

public class CompactPurchaseResponseBuilder {

    public static CompactPurchaseResponse build(Purchase purchase, List<CompactProductQuantityPairResponse> pairs) {
        CompactPurchaseResponse response = new CompactPurchaseResponse();

        response.purchaseCode = purchase.getPurchaseCode();
        response.purchaseDate = purchase.getDate();
        response.totalCost = purchase.getTotalCost();
        response.compactProductQuantityPairs = pairs;

        return response;
    }
}
