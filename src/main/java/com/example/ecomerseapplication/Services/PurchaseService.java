package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Purchase;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    @Autowired
    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public Purchase save(Purchase purchase) {
        return purchaseRepository.save(purchase);
    }

    public List<Purchase> getByCustomer(Customer customer) {
        return purchaseRepository.getByCustomer(customer);
    }
}
