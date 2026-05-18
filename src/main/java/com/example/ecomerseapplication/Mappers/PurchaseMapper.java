package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

public class PurchaseMapper {
//
//    public static PurchaseResponse entityToResponse(Purchase purchase) {
//        PurchaseResponse purchaseResponse = new PurchaseResponse();
//        purchaseResponse.purchaseCode = purchase.getPurchaseCode();
//        purchaseResponse.totalCost = purchase.getTotalCost();
//        purchaseResponse.dateOfPurchase = purchase.getDate();
//
//        SavedPurchaseDetailsResponse savedPurchaseDetailsResponse = new SavedPurchaseDetailsResponse();
//
//        savedPurchaseDetailsResponse.address = purchase.getAddress();
//        savedPurchaseDetailsResponse.contactName = purchase.getContactName();
//        savedPurchaseDetailsResponse.contactNumber = purchase.getContactNumber();
//
//        purchaseResponse.savedPurchaseDetailsResponse = savedPurchaseDetailsResponse;
//
//        return purchaseResponse;
//    }

    public static SuccessfulPurchaseResponse entityToSuccessResponse(Purchase purchase) {
        return new SuccessfulPurchaseResponse(purchase.getPurchaseCode(),
                purchase.getDeliveryStatus().name(),
                purchase.getTotalCost(),
                purchase.getShippingFee(),
                purchase.getPaymentMethod().name());
    }


    public static Page<CompactPurchaseResponse> purchasePageToCompactResponsePage(Page<Purchase> purchasePage,
                                                                                  Map<Long, List<ProductForCompactPurchaseHistoryResponse>> purchaseProductMap) {
        return new PageImpl<>(purchaseListToCompactResponseList(purchasePage.getContent(), purchaseProductMap), purchasePage.getPageable(), purchasePage.getTotalElements());
    }

    public static List<CompactPurchaseResponse> purchaseListToCompactResponseList(List<Purchase> purchases,
                                                                                  Map<Long, List<ProductForCompactPurchaseHistoryResponse>> purchaseProductMap) {
        return purchases
                .stream()
                .map(purchase -> purchaseDataToCompactResponse(purchase, purchaseProductMap.get(purchase.getId())))
                .toList();
    }

    public static CompactPurchaseResponse purchaseDataToCompactResponse(Purchase purchase, List<ProductForCompactPurchaseHistoryResponse> mappedProducts ) {

        return new CompactPurchaseResponse(purchase.getPurchaseCode(),
                purchase.getDate(),
                purchase.getTotalCost(),
                purchase.getShippingFee(),
                purchase.getDeliveryStatus().name(),
                "",
                purchase.getAddress(),
                mappedProducts

        );
    }
}
