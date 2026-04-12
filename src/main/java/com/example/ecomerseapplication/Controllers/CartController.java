package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Auth.helpers.SessionExtractor;
import com.example.ecomerseapplication.Auth.helpers.UserIdExtractor;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.DTOs.requests.ProductForCartRequest;
import com.example.ecomerseapplication.DTOs.responses.CartItemResponse;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Services.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@Validated
@RequestMapping("cart/")
public class CartController {

    private final SessionService sessionService;
    private final UserIdExtractor userIdExtractor;
    private final CustomerService customerService ;
    private final CartProductService cartProductService;
    private final CartService cartService;
    private final ProductService productService;

    public CartController(SessionService sessionService, UserIdExtractor userIdExtractor, CustomerService customerService, CartProductService cartProductService, CartService cartService, ProductService productService) {
        this.sessionService = sessionService;
        this.userIdExtractor = userIdExtractor;
        this.customerService = customerService;
        this.cartProductService = cartProductService;
        this.cartService = cartService;
        this.productService = productService;
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

    @PostMapping("manageQuant")
    public ResponseEntity<?> addToCart(@RequestBody @Valid ProductForCartRequest request) {

        Product product = productService.findByPCode(request.productCode);

        if (!product.isInStock()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ErrorType.OUT_OF_STOCK,
                    "Продуктът не е наличен", HttpStatus.BAD_REQUEST.value(),
                    "Този продукт е изчерпан и не беше добавен в количката"));
        }

        String sessionId = SessionExtractor.getRequestSessionId();
        Session session = sessionService.getById(sessionId);

        if (session.getIsGuest()) {
            return ResponseEntity.ok(cartProductService.addToOrRemoveFromCart(session, product, request.doIncrement));
        }

        else
        {
            String userId = userIdExtractor.getUserId();
            Customer customer = customerService.getByIdWithActivityRefresh(userId);

            return ResponseEntity.ok(cartProductService.addToOrRemoveFromCart(customer, product, request.doIncrement));
        }
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
