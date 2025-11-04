package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.CustomerAccountRequest;
import com.example.ecomerseapplication.DTOs.requests.CustomerProductPairRequest;
import com.example.ecomerseapplication.DTOs.requests.ProductForCartRequest;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Mappers.CustomerCartResponseMapper;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Mappers.PurchaseMapper;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("customer/")
public class CustomerController {

    private final CustomerService customerService;

    private final ProductService productService;

    private final CustomerCartService customerCartService;

    private final PurchaseService purchaseService;

    private final PurchaseCartService purchaseCartService;

    @Autowired
    public CustomerController(CustomerService customerService, ProductService productService, CustomerCartService customerCartService, PurchaseService purchaseService, PurchaseCartService purchaseCartService) {
        this.customerService = customerService;
        this.productService = productService;
        this.customerCartService = customerCartService;
        this.purchaseService = purchaseService;
        this.purchaseCartService = purchaseCartService;
    }

    @PostMapping("registration")
    @Transactional
    public ResponseEntity<String> register(@RequestBody CustomerAccountRequest customerAccountRequest) {
        return customerService.registration(customerAccountRequest);
    }

    @PostMapping("login")
    public ResponseEntity<CustomerResponse> logIn(@RequestBody CustomerAccountRequest customerAccountRequest) {
        return customerService.logIn(customerAccountRequest);
    }

    @PostMapping("addfavourite")
    @Transactional
    public ResponseEntity<String> addProductToFavourites(@RequestBody CustomerProductPairRequest pairRequest) {
        Product product = productService.findByPCode(pairRequest.productCode);

        if (product == null)
            return ResponseEntity.notFound().build();
        return customerService.addProductToFavourites(pairRequest.customerId, product);
    }

    @GetMapping("favourites/p/{page}")
    public ResponseEntity<CompactProductPagedListResponse> getFavourites(@RequestParam long id, @PathVariable int page) {
        Customer customer = customerService.findById(id);
        if (customer == null)
            return ResponseEntity.notFound().build();

        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        CompactProductPagedListResponse productResponse = ProductDTOMapper
                .pagedListToDtoResponse(customer.getFavourites(), pageRequest);

        if (productResponse.content.isEmpty())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(productResponse);
    }

    @PostMapping("addtocart")
    @Transactional
    public ResponseEntity<String> addToCart(@RequestBody ProductForCartRequest request) {
        Product product = productService.findByPCode(request.customerProductPairRequest.productCode);

        if (product == null)
            return ResponseEntity.notFound().build();

        Customer customer = customerService.findById(request.customerProductPairRequest.customerId);

        if (customer == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Няма такъв потребител");

        return customerCartService.addToOrRemoveFromCart(customer, product, request.quantity);
    }

    @DeleteMapping("removefav")
    @Transactional
    public ResponseEntity<String> removeFromFavourites(@RequestBody CustomerProductPairRequest pairRequest) {

        Customer customer = customerService.findById(pairRequest.customerId);

        if (customer == null)
            return ResponseEntity.notFound().build();

        Product product = productService.findByPCode(pairRequest.productCode);

        if (product == null)
            return ResponseEntity.notFound().build();

        return customerService.removeFromFavourites(customer, product);
    }

    @GetMapping("cart")
    public ResponseEntity<CustomerCartResponse> showCart(@RequestParam long id) {
        Customer customer = customerService.findById(id);

        if (customer == null)
            return ResponseEntity.notFound().build();

        List<CustomerCart> customerCarts = customerCartService.cartsByCustomer(customer);

        if (customerCarts.isEmpty())
            return ResponseEntity.notFound().build();

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(CustomerCartResponseMapper.listToResponse(customerCarts));
    }

    @GetMapping("purchase_history")
    public ResponseEntity<List<CompactPurchaseResponse>> showPurchases(@RequestParam long id) {

        Customer customer = customerService.findById(id);

        if (customer == null)
            return ResponseEntity.notFound().build();

        List<Purchase> purchases = purchaseService.getByCustomer(customer);

        List<CompactPurchaseResponse> responses = new ArrayList<>();

        for (Purchase purchase : purchases) {
            List<PurchaseCart> purchaseCarts = purchaseCartService.getByPurchase(purchase);

            if (purchaseCarts.isEmpty())
                continue;

            List<CompactProductQuantityPairResponse> pairs = new ArrayList<>();

            for (PurchaseCart cart : purchaseCarts) {
                CompactProductQuantityPairResponse pair = new CompactProductQuantityPairResponse();
                pair.compactProductResponse = ProductDTOMapper
                        .entityToCompactResponse(cart.getPurchaseCartId().getProduct());
                pair.quantity = cart.getQuantity();

                pairs.add(pair);
            }

            CompactPurchaseResponse compactPurchaseResponse = PurchaseMapper.purchaseDataToResponse(purchase, pairs);

            responses.add(compactPurchaseResponse);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responses);
    }

    @PostMapping("change-passowrd")
    @Transactional
    public ResponseEntity<String> resetPassword(@RequestBody CustomerAccountRequest request) {
        Customer customer = customerService.getByEmail(request.email);

        if (customer==null)
            return ResponseEntity.notFound().build();

        return customerService.passwordUpdate(customer,request.password);
    }
}
