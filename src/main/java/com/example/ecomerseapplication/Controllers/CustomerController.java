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
    private final FavoriteOfCustomerService favoriteOfCustomerService;

    @Autowired
    public CustomerController(CustomerService customerService,
                              ProductService productService,
                              CustomerCartService customerCartService,
                              PurchaseService purchaseService,
                              PurchaseCartService purchaseCartService,
                              KeycloakService keycloakService,
                              UserIdExtractor userIdExtractor, FavoriteOfCustomerService favoriteOfCustomerService) {

        this.customerService = customerService;
        this.productService = productService;
        this.customerCartService = customerCartService;
        this.purchaseService = purchaseService;
        this.purchaseCartService = purchaseCartService;
        this.keycloakService = keycloakService;
        this.userIdExtractor = userIdExtractor;
        this.favoriteOfCustomerService = favoriteOfCustomerService;
    }

    @PostMapping("favorite/add")
//    @Transactional
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> addProductToFavourites(@RequestParam String productCode) {

        String userId = userIdExtractor.getUserId();

        Product product = productService.findByPCode(productCode);
        Customer customer = customerService.getByKID(userId);

        if (product == null || customer == null)
            return ResponseEntity.notFound().build();

//        return customerService.addProductToFavourites(userId, product);
        return favoriteOfCustomerService.addToFavorite(customer, product);
    }

    @GetMapping("favourites/p/{page}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> getFavourites(@PathVariable int page) {

        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByKID(userId);
        if (customer == null)
            return ResponseEntity.notFound().build();

        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        PageResponse<CompactProductResponse> response = favoriteOfCustomerService.getFromFavourites(customer, pageRequest);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @DeleteMapping("favourites/remove/{productCode}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeFavFromProdPage(@PathVariable String productCode) {

//        System.out.println("In remove favorite from prod page:");

        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByKID(userId);
        Product product = productService.findByPCode(productCode);
        if (customer == null || product == null) {
            System.out.println("customer or product not found");
            return ResponseEntity.badRequest().build();
        }

        return favoriteOfCustomerService.removeFromFavorites(customer, product);
    }

    @DeleteMapping("favorite/remove/single")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeFromFavourites(@RequestBody RemoveOneFavRequest request) {

        if (NullFieldChecker.hasNullFields(request)) {
            System.out.println("Null fields:\n" + NullFieldChecker.getNullFields(request));
            return ResponseEntity.badRequest().build();
        }

//        System.out.println("Single delete request: "+request);

        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByKID(userId);
        Product product = productService.findByPCode(request.productCode());

        if (customer == null || product == null)
            return ResponseEntity.notFound().build();

        return favoriteOfCustomerService.removeFromFavoritesWRefetch(
                customer,
                product,
                request.currentPage());
    }


    @DeleteMapping("favorite/remove/batch")
    @Transactional
    public ResponseEntity<?> removeFromFavourites(@RequestBody RemoveFavBatchRequest request) {

        System.out.println("Batch delete request: "+request);

        String userId = userIdExtractor.getUserId();

        if (NullFieldChecker.hasNullFields(request)) {
            System.out.println("Null fields from request: " + NullFieldChecker.getNullFields(request));
            return ResponseEntity.badRequest().build();
        }
        Customer customer = customerService.getByKID(userId);

        if (customer == null) {
            System.out.println("customer not found");
            return ResponseEntity.notFound().build();
        }

        return favoriteOfCustomerService.removeFavoritesBatch(customer, request.productCodes(), request.currentPage());
    }

    @PostMapping("cart/manageQuant")
    @Transactional
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> addToCart(@RequestBody ProductForCartRequest request) {

//        System.out.println("REQUEST: "+request);

        if (NullFieldChecker.hasNullFields(request)) {

            System.out.println("Null fields:\n" + NullFieldChecker.getNullFields(request));

            return ResponseEntity.badRequest().build();
        }

        Product product = productService.findByPCode(request.productCode);

        if (product == null)
            return ResponseEntity.notFound().build();

        if (!product.isInStock()) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.OUT_OF_STOCK,
                    "Продуктът не е наличен", HttpStatus.BAD_REQUEST.value(),
                    "Този продукт е изчерпан и не беше добавен в количката"));
        }

        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByKID(userId);

        if (customer == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Няма такъв потребител");

        return customerCartService.addToOrRemoveFromCart(customer, product, request.doIncrement);
    }

    @PostMapping("cart/add/batch")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> addBatchToCart(@RequestBody List<String> productCodes) {

        if (productCodes.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByKID(userId);

        if (customer == null) {
            System.out.println("customer not found");
            return ResponseEntity.notFound().build();
        }

        List<Product> requestProducts = productService.getByCodes(productCodes);

        if (requestProducts.isEmpty()) {
            System.out.println("No products found from request");
            return ResponseEntity.notFound().build();
        }

        try {
            return customerCartService.addBatchToCart(customer, requestProducts);
        } catch (Exception e) {
            System.out.println("Error adding product batch to cart: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("cart/remove/{productCode}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeFromCart(@PathVariable String productCode) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByKID(userId);

        if (productCode == null || customer == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
           return customerCartService.removeFromCartWFetch(customer, productCode);
        } catch (Exception e) {
            System.out.println("Error removing from cart: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("cart/remove/batch")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeBatchFromCart(@RequestBody List<String> productCodes) {

        System.out.println("Product codes: " + productCodes);

       if (productCodes.isEmpty()) {
           return ResponseEntity.badRequest().build();
       }


       String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByKID(userId);

        try {
            return customerCartService.removeBatchFromCartWFetch(customer, productCodes);
        } catch (Exception e) {
            System.out.println("Error removing product batch from cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }


    @GetMapping("cart")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> showCart() {
//        Customer customer = customerService.findById(id);

        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByKID(userId);

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
    public ResponseEntity<CustomerResponse> getCustomerInfo() {
        String userId = userIdExtractor.getUserId();

//        System.out.println("userId: " + userId);

        Customer customer = customerService.getByKID(userId);

        if (customer == null) {
            System.out.println("customer not found");
            return ResponseEntity.notFound().build();
        }

//        System.out.println(customer.getCustomerPfp());

        String userRole = keycloakService.getRoleByUserId(userId);

        return ResponseEntity.ok(new CustomerResponse(
                        customer.getId(),
                        customer.getFirstName() + " " + customer.getLastName(),
                        customer.getCustomerPfp(),
                        userRole
//                customer.getId()
                )
        );
    }

    @GetMapping("getPfp")
    @PreAuthorize("hasRole(@roles.customer())")
    public  ResponseEntity<String> getPfp() {
        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByKID(userId);
        if (customer == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(customer.getCustomerPfp());
    }

}
