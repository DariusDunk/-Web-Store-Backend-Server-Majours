package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> getCustomerByKeycloakId(String keycloakId);
}
