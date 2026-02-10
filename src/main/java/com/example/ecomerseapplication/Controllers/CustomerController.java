package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Auth.helpers.UserIdExtractor;
import com.example.ecomerseapplication.DTOs.requests.*;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Mappers.PurchaseMapper;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Services.*;
import com.example.ecomerseapplication.Utils.NullFieldChecker;
import com.example.ecomerseapplication.enums.UserRole;
import org.keycloak.common.VerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final KeycloakService keycloakService;
    private final UserIdExtractor userIdExtractor;


    @Autowired
    public CustomerController(CustomerService customerService,
                              ProductService productService,
                              CustomerCartService customerCartService,
                              PurchaseService purchaseService,
                              PurchaseCartService purchaseCartService,
                              KeycloakService keycloakService,
                              UserIdExtractor userIdExtractor) {

        this.customerService = customerService;
        this.productService = productService;
        this.customerCartService = customerCartService;
        this.purchaseService = purchaseService;
        this.purchaseCartService = purchaseCartService;
        this.keycloakService = keycloakService;
        this.userIdExtractor = userIdExtractor;
    }

//    @PostMapping("registration")
//    @Transactional
//    public ResponseEntity<String> register(@RequestBody CustomerAccountRequest customerAccountRequest) {
//        return customerService.registration(customerAccountRequest);
//    }

//    @PostMapping("login")
//    public ResponseEntity<CustomerResponse> logIn(@RequestBody CustomerAccountRequest customerAccountRequest) {
//        return customerService.logIn(customerAccountRequest);
//    }

    @PostMapping("favorite/add")
    @Transactional
    @PreAuthorize("hasRole(@roles.customer())")
//    public ResponseEntity<?> addProductToFavourites(@RequestBody CustomerProductPairRequest pairRequest) {
    public ResponseEntity<?> addProductToFavourites(@RequestParam String productCode) {

//        if (NullFieldChecker.hasNullFields(pairRequest)) {
//            System.out.println("Null fields:\n" + NullFieldChecker.getNullFields(pairRequest));
//            return ResponseEntity.badRequest().build();
//        }

        String userId = userIdExtractor.getUserId();

        Product product = productService.findByPCode(productCode);

        if (product == null)
            return ResponseEntity.notFound().build();
        return customerService.addProductToFavourites(userId, product);
    }

    @GetMapping("favourites/{id}/p/{page}")//PAGING
    public ResponseEntity<?> getFavourites(@PathVariable long id, @PathVariable int page) {
        Customer customer = customerService.findById(id);
        if (customer == null)
            return ResponseEntity.notFound().build();

        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        PageResponse<CompactProductResponse> response = productService.getFromFavourites(customer,pageRequest);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @PostMapping("cart/add")
    @Transactional
    public ResponseEntity<?> addToCart(@RequestBody ProductForCartRequest request) {

//        System.out.println("REQUEST: "+request);

        if (NullFieldChecker.hasNullFields(request)) {

            System.out.println("Null fields:\n"+NullFieldChecker.getNullFields(request));

            return ResponseEntity.badRequest().build();
        }

        Product product = productService.findByPCode(request.customerProductPairRequest.productCode);

        if (product == null)
            return ResponseEntity.notFound().build();

        if (!product.isInStock()) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.OUT_OF_STOCK,
                    "Продуктът не е наличен", HttpStatus.BAD_REQUEST.value(),
                    "Този продукт е изчерпан и не беше добавен в количката"));
        }

        Customer customer = customerService.findById(request.customerProductPairRequest.customerId);

        if (customer == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Няма такъв потребител");

        return customerCartService.addToOrRemoveFromCart(customer, product, request.doIncrement);
    }

    @PostMapping("cart/add/batch")
    public ResponseEntity<?> addBatchToCart(@RequestBody BatchProductUserRequest request)
    {

        if (NullFieldChecker.hasNullFields(request)) {
            System.out.println("Null fields:\n"+NullFieldChecker.getNullFields(request));
            return ResponseEntity.badRequest().build();
        }

        Customer customer = customerService.findById(request.customerId());

        if (customer == null) {
            System.out.println("customer not found");
            return ResponseEntity.notFound().build();
        }

        List<Product> requestProducts = productService.getByCodes(request.productCodes());

        if (requestProducts.isEmpty()) {
            System.out.println("No products found from request");
            return ResponseEntity.notFound().build();
        }

        try
        {
          return customerCartService.addBatchToCart(customer, requestProducts);
        }
        catch (Exception e)
        {
            System.out.println("Error adding product batch to cart: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("cart/remove")
    @Transactional
    public ResponseEntity<?> removeFromCart(@RequestBody CustomerProductPairRequest pairRequest) {

        if (NullFieldChecker.hasNullFields(pairRequest)) {

            System.out.println("Null fields from request:" + NullFieldChecker.getNullFields(pairRequest));

            return ResponseEntity.badRequest().build();
        }

        Customer customer = customerService.findById(pairRequest.customerId);
        try
        {
            customerCartService.removeFromCart(customer, pairRequest.productCode);
        }
        catch (Exception e)
        {
            System.out.println("Error removing from cart: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("cart/remove/batch")
    @Transactional
    public ResponseEntity<?> removeBatchFromCart(@RequestBody BatchProductUserRequest request) {

        if (NullFieldChecker.hasNullFields(request)) {
            System.out.println("Null fields from request:" + NullFieldChecker.getNullFields(request));
            return ResponseEntity.badRequest().build();
        }

        Customer customer = customerService.findById(request.customerId());

        try
        {
           return customerCartService.removeBatchFromCart(customer, request.productCodes());
        }
        catch (Exception e)
        {
            System.out.println("Error removing product batch from cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @DeleteMapping("favorite/remove")
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

    @DeleteMapping("favorite/remove/batch")
    @Transactional
    public ResponseEntity<?> removeFromFavourites(@RequestBody BatchProductUserRequest request) {

//        System.out.println(request);

        if (NullFieldChecker.hasNullFields(request)) {

            System.out.println("Null fields from request: "+ NullFieldChecker.getNullFields(request));

            return ResponseEntity.badRequest().build();
        }

        Customer customer = customerService.findById(request.customerId());

        if (customer == null) {
            System.out.println("customer not found");
            return ResponseEntity.notFound().build();
        }

        try{
            customerService.removeFavoritesBatch(customer, request.productCodes());

            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            System.out.println("Error deleting favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping("cart")
    public ResponseEntity<?> showCart(@RequestParam long id) {
        Customer customer = customerService.findById(id);

        if (customer == null)
            return ResponseEntity.notFound().build();

        List<CartItemResponse> customerCarts = customerCartService.getCartDtoByCustomer(customer);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(customerCarts);
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
//                ProductDTOMapper.addReviewsCountToCompactResponse(pair.compactProductResponse);
                pair.quantity = cart.getQuantity();

                pairs.add(pair);
            }

            CompactPurchaseResponse compactPurchaseResponse = PurchaseMapper.purchaseDataToResponse(purchase, pairs);

            responses.add(compactPurchaseResponse);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responses);
    }

//    @PostMapping("change-passowrd")
//    @Transactional
//    public ResponseEntity<String> resetPassword(@RequestBody CustomerAccountRequest request) {TODO syzdai keycloak ekvivalent
//        Customer customer = customerService.getByEmail(request.email);
//
//        if (customer == null)
//            return ResponseEntity.notFound().build();
//
//        return customerService.passwordUpdate(customer, request.password);
//    }



    @GetMapping("me")
    public ResponseEntity<CustomerResponse> getCustomerInfo()
    {
        String userId = userIdExtractor.getUserId();

//        System.out.println("userId: " + userId);

        Customer customer = customerService.getByKID(userId);

        if (customer == null) {
            System.out.println("customer not found");
            return ResponseEntity.notFound().build();
        }

        String userRole = keycloakService.getRoleByUserId(userId);

        return ResponseEntity.ok(new CustomerResponse(
                             customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                             customer.getCustomerPfp(),
                userRole, customer.getId()
                )
        );
    }

    @GetMapping("getPfp")
    public ResponseEntity<String> getUserPfp(@RequestParam int id) {
        String pfp = customerService.getPfpUrl(id);

        if (pfp== null|| pfp.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(pfp);
    }
}
