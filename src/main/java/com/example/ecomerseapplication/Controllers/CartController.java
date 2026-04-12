package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Auth.helpers.SessionExtractor;
import com.example.ecomerseapplication.Auth.helpers.UserIdExtractor;
import com.example.ecomerseapplication.DTOs.responses.CartItemResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Services.CartProductService;
import com.example.ecomerseapplication.Services.CartService;
import com.example.ecomerseapplication.Services.CustomerService;
import com.example.ecomerseapplication.Services.SessionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@Validated
@RequestMapping("/cart")
public class CartController {

    private final SessionService sessionService;
    private final UserIdExtractor userIdExtractor;
    private final CustomerService customerService ;
    private final CartProductService cartProductService;
    private final CartService cartService;

    public CartController(SessionService sessionService, UserIdExtractor userIdExtractor, CustomerService customerService, CartProductService cartProductService, CartService cartService) {
        this.sessionService = sessionService;
        this.userIdExtractor = userIdExtractor;
        this.customerService = customerService;
        this.cartProductService = cartProductService;
        this.cartService = cartService;
    }

    @GetMapping("get")
    public ResponseEntity<?> showCart() {

        String sessionId = SessionExtractor.getRequestSessionId();
        Session session = sessionService.getById(sessionId);

        if (session.getIsGuest()) {
            return ResponseEntity.ok(cartProductService.getCartDtoBySession(session));
        }

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);
        List<CartItemResponse> customerCarts = cartProductService.getCartDtoByCustomer(customer);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(customerCarts);
    }

    @GetMapping("summary")
    public ResponseEntity<?> getCartSummary() {

        String sessionId = SessionExtractor.getRequestSessionId();
        Session session = sessionService.getById(sessionId);

        if (session.getIsGuest()) {
            return ResponseEntity.ok(cartProductService.getSummary(session));
        }

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);
        return ResponseEntity.ok(cartProductService.getSummary(customer));

    }

}
