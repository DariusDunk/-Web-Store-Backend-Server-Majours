package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Repositories.CustomerRepository;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import jakarta.validation.constraints.Positive;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PurchaseRepository purchaseRepository;
    private final SessionService sessionService;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PurchaseRepository purchaseRepository, SessionService sessionService) {
        this.customerRepository = customerRepository;
        this.purchaseRepository = purchaseRepository;
        this.sessionService = sessionService;
    }

//    public void createByRepresentation(UserRepresentation user, String userId) {
//        Customer customer = new Customer();
//        customer.setEmail(user.getEmail());
//        customer.setFirstName(user.getFirstName());
//        customer.setLastName(user.getLastName());
//        customer.setRegistrationDate(LocalDate.now());
//        customer.setKeycloakId(userId);
//        customerRepository.save(customer);
//    }

    public Customer getById(String userId) {
        return customerRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("No user found with ID "+ userId));
    }

    public Customer getByIdWithActivityRefresh(String userId) {

        Customer customer = getById(userId);
        sessionService.updateActivity();
        return customer;

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
