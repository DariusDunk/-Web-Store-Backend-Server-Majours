package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
//    private final PurchaseRepository purchaseRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository
//                           PurchaseRepository purchaseRepository,
    ) {
        this.customerRepository = customerRepository;
//        this.purchaseRepository = purchaseRepository;
    }

    public Customer getById(String userId) {
        return customerRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("No user found with ID "+ userId));
    }

    public Customer save(Customer customer) {
        try {
            return customerRepository.save(customer);
        } catch (Exception e) {
            System.out.println("Error saving customer in DB: " + e.getMessage());
            throw e;
        }
    }

}
