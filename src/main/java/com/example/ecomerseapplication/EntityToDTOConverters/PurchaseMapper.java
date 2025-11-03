package com.example.ecomerseapplication.EntityToDTOConverters;

import com.example.ecomerseapplication.DTOs.SavedRecipientDetailsRequest;
import com.example.ecomerseapplication.DTOs.SavedPurchaseDetailsResponse;
import com.example.ecomerseapplication.DTOs.responses.PurchaseResponse;
import com.example.ecomerseapplication.Entities.Purchase;
import com.example.ecomerseapplication.Others.PurchaseCodeGenerator;

public class PurchaseMapper {

    public static Purchase requestToEntity(SavedRecipientDetailsRequest savedRecipientDetailsRequest) {
        Purchase purchase = new Purchase();
        purchase.setPurchaseCode(PurchaseCodeGenerator.generateCode(purchase.getDate()));
        purchase.setAddress(savedRecipientDetailsRequest.address);
        purchase.setContactName(savedRecipientDetailsRequest.contactName);
        purchase.setContactNumber(savedRecipientDetailsRequest.contactNumber);

        return purchase;
    }
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

        /*RecipientDetailRequest recipientDetailRequest = new RecipientDetailRequest();
        recipientDetailRequest.address = purchase.getAddress();
        recipientDetailRequest.contactName= purchase.getContactName();
        recipientDetailRequest.contactNumber = purchase.getContactNumber();

        purchaseResponse.recipientDetailRequest = recipientDetailRequest;*/

        return purchaseResponse;
    }
}
