package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Cart;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getById(Long id) {
        return cartRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cart owner not found"));
    }

    public Cart getOrCreateByCustomer(Customer customer) {
        return cartRepository.getCartOwnerByCustomer(customer).orElseThrow(()->new ResourceNotFoundException("Cart not found for customer: " + customer.getKeycloakId()));
    }
}
