package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Cart;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Others.GlobalConstants;
import org.springframework.stereotype.Service;

@Service
public class SessionCartService {

    private final CartService cartService;
    private final SessionService sessionService;

    public SessionCartService(CartService cartService, SessionService sessionService) {
        this.cartService = cartService;
        this.sessionService = sessionService;
    }

    public Cart getOrCreateSessionCart(Session session) {

        Cart cart = cartService.getBySession(session).orElse(null);

        if (cart == null) {
            cart = new Cart(session);
            session.markAsCartActive(GlobalConstants.GUEST_SESSION_TTL_DAYS);
            sessionService.save(session);
        }

        return cart;

    }

}
