package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.Purchase;

import java.util.List;

public class PurchaseMapper {

//    public static Purchase requestToEntity(SavedRecipientDetailsRequest savedRecipientDetailsRequest) {
//        Purchase purchase = new Purchase();
//        purchase.setPurchaseCode(PurchaseCodeGenerator.generateCode(purchase.getDate()));
//        purchase.setAddress(savedRecipientDetailsRequest.address);
//        purchase.setContactName(savedRecipientDetailsRequest.contactName);
//        purchase.setContactNumber(savedRecipientDetailsRequest.contactNumber);
//
//        return purchase;
//    }
    public static PurchaseResponse entityToResponse(Purchase purchase) {
        PurchaseResponse purchaseResponse = new PurchaseResponse();
        purchaseResponse.purchaseCode = purchase.getPurchaseCode();
        purchaseResponse.totalCost = purchase.getTotalCost();
        purchaseResponse.dateOfPurchase = purchase.getDate();

        SavedPurchaseDetailsResponse savedPurchaseDetailsResponse = new SavedPurchaseDetailsResponse();

        savedPurchaseDetailsResponse.address = purchase.getAddress();
        savedPurchaseDetailsResponse.contactName = purchase.getContactName();
        savedPurchaseDetailsResponse.contactNumber = purchase.getContactNumber();

        purchaseResponse.savedPurchaseDetailsResponse = savedPurchaseDetailsResponse;

        return purchaseResponse;
    }

    public static SuccessfulPurchaseResponse entityToSuccessResponse(Purchase purchase) {
        return new SuccessfulPurchaseResponse(purchase.getPurchaseCode(),
                purchase.getDeliveryStatus().name(),
                purchase.getTotalCost(),
                purchase.getShippingFee(),
                purchase.getPaymentMethod().name());
    }

    public static CompactPurchaseResponse purchaseDataToResponse(Purchase purchase, List<CompactProductQuantityPairResponse> pairs) {
        CompactPurchaseResponse response = new CompactPurchaseResponse();

        response.purchaseCode = purchase.getPurchaseCode();
        response.purchaseDate = purchase.getDate();
        response.totalCost = purchase.getTotalCost();
        response.compactProductQuantityPairs = pairs;

        return response;
    }
}
