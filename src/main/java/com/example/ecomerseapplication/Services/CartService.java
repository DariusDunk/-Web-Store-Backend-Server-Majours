package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Cart;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getById(Long id) {
        return cartRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    public Cart getOrCreateByCustomer(Customer customer) {
        return cartRepository.getCartOwnerByCustomer(customer)
                .orElseGet(() -> cartRepository.save(new Cart(customer)));
    }

    public void save(Cart cart) {
        cartRepository.save(cart);
    }

    public Cart getOrCreateBySession(Session session) {
        return cartRepository.getBySession((session))
                .orElseGet(() -> cartRepository.save(new Cart(session)));
    }

//    public void deleteCart(Cart cart) {
//        cartRepository.delete(cart);
//    }

    public void deleteCartsBySessions(List<Session> sessions) {

        List<String> sessionIds = sessions.stream().map(Session::getSessionId).toList();

        cartRepository.deleteBySessions(sessionIds);
    }


}
