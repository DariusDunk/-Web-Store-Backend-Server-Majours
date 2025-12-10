package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CompositeIdClasses.CustomerCartId;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.CustomerCart;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Repositories.CustomerCartRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerCartService {


    private final CustomerCartRepository customerCartRepository;

    @Autowired
    public CustomerCartService(CustomerCartRepository customerCartRepository) {
        this.customerCartRepository = customerCartRepository;
    }

    @Transactional
    public ResponseEntity<String> addToOrRemoveFromCart(Customer customer, Product product, short quantity) {

        CustomerCartId cartId = new CustomerCartId(product, customer);

        CustomerCart customerCart = customerCartRepository.findById(cartId).orElse(null);

        if (customerCart == null) {
            customerCart = new CustomerCart(cartId, quantity);
            customerCartRepository.save(customerCart);
            return ResponseEntity.status(HttpStatus.CREATED).body("Успешно добавен в количката!");
        }

        if (quantity == 0) {
            customerCartRepository.deleteById(cartId);
            return ResponseEntity.status(HttpStatus.OK).body("Успешно премахнат от количката!");
        }

        customerCartRepository.updateQuantity(quantity, customer.getId(), product.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body("Промяната е успешна");

    }

    public boolean cartExists(Customer customer, Product product) {
        return customerCartRepository.existsByCustomerCartId(new CustomerCartId(product, customer));
    }

    public List<CustomerCart> cartsByCustomer(Customer customer) {
        return customerCartRepository.findByCustomer(customer);
    }

    @Transactional
    public void clearCart(Customer customer) {
        customerCartRepository.deleteAllByCustomer(customer);
    }

    public void removeFromCart(Customer customer, String productCode) {
        List<CustomerCart> cart = cartsByCustomer(customer);

        if (cart.isEmpty()) throw new IllegalArgumentException("Cart is empty!");

        CustomerCart cartItem = cart.stream().filter(c -> c.getCustomerCartId().getProduct().getProductCode().equals(productCode)).findFirst().orElseThrow();

        customerCartRepository.delete(cartItem);

    }
}
