package com.example.ecomerseapplication.EntityToDTOConverters;

import com.example.ecomerseapplication.DTOs.responses.SavedPurchaseDetailsResponse;
import com.example.ecomerseapplication.Entities.SavedPurchaseDetails;

public class SavedPurchaseDetailsMapper {

    public static SavedPurchaseDetailsResponse entityToResponse(SavedPurchaseDetails inputEntity) {
        SavedPurchaseDetailsResponse dto = new SavedPurchaseDetailsResponse();

        dto.contactName = inputEntity.getContactName();
        dto.contactNumber = inputEntity.getContactNumber();
        dto.address = inputEntity.getAddress();

        return dto;
    }
}
