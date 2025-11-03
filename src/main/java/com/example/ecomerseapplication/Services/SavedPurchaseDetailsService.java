package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.SavedPurchaseDetailsResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.SavedPurchaseDetails;
import com.example.ecomerseapplication.Mappers.SavedPurchaseDetailsMapper;
import com.example.ecomerseapplication.Repositories.SavedPurchaseDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SavedPurchaseDetailsService {

    @Autowired
    SavedPurchaseDetailsRepo savedPurchaseDetailsRepo;

    public ResponseEntity<String> saveDetails(SavedPurchaseDetails purchaseDetails) {
        savedPurchaseDetailsRepo.save(purchaseDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body("Информацията е запазена!");
    }

    public ResponseEntity<SavedPurchaseDetailsResponse> getByCustomer(Customer customer) {

        SavedPurchaseDetails purchaseDetails = savedPurchaseDetailsRepo.getByCustomer(customer).orElse(null);

        if (purchaseDetails==null)
            return ResponseEntity.notFound().build();

        SavedPurchaseDetails savedPurchaseDetails = savedPurchaseDetailsRepo.getByCustomer(customer).orElse(null);

        if (savedPurchaseDetails == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(SavedPurchaseDetailsMapper
                        .entityToResponse(savedPurchaseDetails));
    }
}
