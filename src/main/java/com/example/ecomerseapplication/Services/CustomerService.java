package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.Repositories.CustomerRepository;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PurchaseRepository purchaseRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PurchaseRepository purchaseRepository) {
        this.customerRepository = customerRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public Customer findById(long id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Long getLongIdByKId(String userId) {
        return customerRepository.getIdByKeycloakId(userId);
    }

    public void createByRepresentation(UserRepresentation user, String userId) {
        Customer customer = new Customer();
        customer.setEmail(user.getEmail());
        customer.setFirstName(user.getFirstName());
        customer.setLastName(user.getLastName());
        customer.setRegistrationDate(LocalDate.now());
        customer.setKeycloakId(userId);
        customerRepository.save(customer);
    }

    public Customer getByKID(String userId) {
        return customerRepository.getCustomerByKeycloakId(userId);//TODO kato migrira6 zameni izpolzvaniqta sys getById s novoto id
    }
}
