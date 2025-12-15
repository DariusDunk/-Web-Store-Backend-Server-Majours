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

import java.util.List;

@Service
public class SavedPurchaseDetailsService {

    private final SavedPurchaseDetailsRepo savedPurchaseDetailsRepo;

    @Autowired
    public SavedPurchaseDetailsService(SavedPurchaseDetailsRepo savedPurchaseDetailsRepo) {
        this.savedPurchaseDetailsRepo = savedPurchaseDetailsRepo;
    }

    public ResponseEntity<String> saveDetails(SavedPurchaseDetails purchaseDetails) {
        savedPurchaseDetailsRepo.save(purchaseDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body("Информацията е запазена!");
    }

    public ResponseEntity<?> getByCustomer(Customer customer) {

        List<SavedPurchaseDetails> savedPurchaseDetails = savedPurchaseDetailsRepo.getByCustomer(customer.getKeycloakId());

        if (savedPurchaseDetails == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(savedPurchaseDetails.stream().map(SavedPurchaseDetailsMapper::entityToResponse));
    }
}
