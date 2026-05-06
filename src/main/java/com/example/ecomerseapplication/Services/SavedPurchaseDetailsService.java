package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.SavedPurchaseDetailsResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.SavedPurchaseDetails;
import com.example.ecomerseapplication.Mappers.SavedPurchaseDetailsMapper;
import com.example.ecomerseapplication.Repositories.SavedPurchaseDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SavedPurchaseDetailsService {

    private final SavedPurchaseDetailsRepository savedPurchaseDetailsRepository;

    @Autowired
    public SavedPurchaseDetailsService(SavedPurchaseDetailsRepository savedPurchaseDetailsRepository) {
        this.savedPurchaseDetailsRepository = savedPurchaseDetailsRepository;
    }

    public void saveDetails(SavedPurchaseDetails purchaseDetails) {
        savedPurchaseDetailsRepository.save(purchaseDetails);
    }

    public SavedPurchaseDetails getById(String customerId) {
        return savedPurchaseDetailsRepository.getByCustomer(customerId).orElseThrow(() -> new ResourceNotFoundException("Saved purchase details not found for id: " + customerId));
    }

    public SavedPurchaseDetailsResponse getByCustomer(Customer customer) {

        try
        {
            SavedPurchaseDetails savedPurchaseDetails = getById(customer.getKeycloakId());
            return SavedPurchaseDetailsMapper.entityToResponse(savedPurchaseDetails);
        }
        catch (ResourceNotFoundException e)
        {
            return null;
        }
    }
}
