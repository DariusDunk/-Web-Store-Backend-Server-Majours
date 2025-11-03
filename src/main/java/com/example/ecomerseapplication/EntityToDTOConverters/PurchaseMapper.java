package com.example.ecomerseapplication.EntityToDTOConverters;

import com.example.ecomerseapplication.DTOs.RecipientDetailResponse;
import com.example.ecomerseapplication.DTOs.responses.PurchaseResponse;
import com.example.ecomerseapplication.Entities.Purchase;
import com.example.ecomerseapplication.Others.PurchaseCodeGenerator;

public class PurchaseMapper {

    public static Purchase requestToEntity(RecipientDetailResponse recipientDetailResponse) {
        Purchase purchase = new Purchase();
        purchase.setPurchaseCode(PurchaseCodeGenerator.generateCode(purchase.getDate()));
        purchase.setAddress(recipientDetailResponse.address);
        purchase.setContactName(recipientDetailResponse.contactName);
        purchase.setContactNumber(recipientDetailResponse.contactNumber);

        return purchase;
    }
    public static PurchaseResponse entityToResponse(Purchase purchase) {
        PurchaseResponse purchaseResponse = new PurchaseResponse();
        purchaseResponse.purchaseCode = purchase.getPurchaseCode();
        purchaseResponse.totalCost = purchase.getTotalCost();
        purchaseResponse.dateOfPurchase = purchase.getDate();

        RecipientDetailResponse recipientDetailResponse = new RecipientDetailResponse();
        recipientDetailResponse.address = purchase.getAddress();
        recipientDetailResponse.contactName= purchase.getContactName();
        recipientDetailResponse.contactNumber = purchase.getContactNumber();

        purchaseResponse.recipientDetailResponse = recipientDetailResponse;

        return purchaseResponse;
    }
}
