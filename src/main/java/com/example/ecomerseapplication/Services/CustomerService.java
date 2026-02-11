package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Repositories.CustomerRepository;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
