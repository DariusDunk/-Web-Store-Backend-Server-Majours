package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.Entities.Cart;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> getCartOwnerByCustomer(Customer customer);

    Optional<Cart> getBySession(Session session);
}
