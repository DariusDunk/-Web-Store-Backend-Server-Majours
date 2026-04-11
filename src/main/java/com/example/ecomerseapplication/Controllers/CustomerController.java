package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Auth.helpers.UserIdExtractor;
import com.example.ecomerseapplication.DTOs.requests.*;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Validated
@RequestMapping("customer/")
public class CustomerController {

    private final CustomerService customerService;
    private final ProductService productService;
    private final CartProductService cartProductService;
//    private final PurchaseService purchaseService;
//    private final PurchaseCartService purchaseCartService;
    private final KeycloakService keycloakService;
    private final UserIdExtractor userIdExtractor;
    private final FavoriteOfCustomerService favoriteOfCustomerService;

    @Autowired
    public CustomerController(CustomerService customerService,
                              ProductService productService,
                              CartProductService cartProductService,
//                              PurchaseService purchaseService,
//                              PurchaseCartService purchaseCartService,
                              KeycloakService keycloakService,
                              UserIdExtractor userIdExtractor, FavoriteOfCustomerService favoriteOfCustomerService) {

        this.customerService = customerService;
        this.productService = productService;
        this.cartProductService = cartProductService;
//        this.purchaseService = purchaseService;
//        this.purchaseCartService = purchaseCartService;
        this.keycloakService = keycloakService;
        this.userIdExtractor = userIdExtractor;
        this.favoriteOfCustomerService = favoriteOfCustomerService;
    }

 
    @PostMapping("favorite/add/{productCode}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> addProductToFavourites(@PathVariable @NotBlank String productCode) {

        String userId = userIdExtractor.getUserId();
        Product product = productService.findByPCode(productCode);
        Customer customer = customerService.getByIdWithActivityRefresh(userId);

        favoriteOfCustomerService.addToFavorite(customer, product);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("favourites/p/{page}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> getFavourites(@PathVariable int page) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);
        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        PageResponse<CompactProductResponse> response = favoriteOfCustomerService.getFromCustomerPaged(customer, pageRequest);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }

    
    @DeleteMapping("favourites/remove/{productCode}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeFavFromProdPage(@PathVariable String productCode) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);
        Product product = productService.findByPCode(productCode);

        favoriteOfCustomerService.removeFromFavorites(customer, product);

        return ResponseEntity.noContent().build();
    }

    
    @DeleteMapping("favorite/remove/single")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeFromFavourites(@RequestBody @Valid RemoveOneFavRequest request) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);
        Product product = productService.findByPCode(request.productCode());

        return ResponseEntity.ok(favoriteOfCustomerService.removeFromFavoritesWRefetch(
                customer,
                product,
                request.currentPage()));
    }

    
    @DeleteMapping("favorite/remove/batch")
    @Transactional
    public ResponseEntity<?> removeFromFavouritesBatch(@RequestBody @Valid RemoveFavBatchRequest request) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);

        return ResponseEntity.ok(favoriteOfCustomerService.removeFavoritesBatch(
                customer,
                request.productCodes(),
                request.currentPage()));
    }

    
    @PostMapping("cart/manageQuant")
    @Transactional
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> addToCart(@RequestBody @Valid ProductForCartRequest request) {

//        System.out.println("REQUEST: "+request);

        Product product = productService.findByPCode(request.productCode);

        if (!product.isInStock()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ErrorType.OUT_OF_STOCK,
                    "Продуктът не е наличен", HttpStatus.BAD_REQUEST.value(),
                    "Този продукт е изчерпан и не беше добавен в количката"));
        }

        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByIdWithActivityRefresh(userId);

        return ResponseEntity.ok(cartProductService.addToOrRemoveFromCart(customer, product, request.doIncrement));
    }

    @PostMapping("cart/add/quantity")
    public ResponseEntity<?> addQuantityToCart(@RequestBody @Valid ProductQuantityForCartRequest request) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);
        Product product = productService.findByPCode(request.productCode());

        return ResponseEntity.ok(cartProductService.addQuantityToCart(product, request.quantity(), customer));
    }


    @PostMapping("cart/add/batch")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> addBatchToCart(@RequestBody @NotEmpty List<String> productCodes) {

        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getByIdWithActivityRefresh(userId);

        List<Product> requestProducts = productService.getByCodes(productCodes);

        return ResponseEntity.status(HttpStatus.CREATED).body(cartProductService.addBatchToCart(customer, requestProducts));

    }

    @DeleteMapping("cart/remove/{productCode}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeFromCart(@PathVariable String productCode) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);

        try {
            return ResponseEntity.ok(cartProductService.removeFromCartWFetch(customer, productCode));
        } catch (Exception e) {
            System.out.println("Error removing from cart: " + e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("cart/remove/batch")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeBatchFromCart(@RequestBody @NotEmpty List<String> productCodes) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);

        try {
            return ResponseEntity.ok(cartProductService.removeBatchFromCartWFetch(customer, productCodes));
        } catch (Exception e) {
            System.out.println("Error removing product batch from cart: " + e.getMessage());
            throw e;
        }
    }

    
    @GetMapping("cart")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> showCart() {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);
        List<CartItemResponse> customerCarts = cartProductService.getCartDtoByCustomer(customer);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(customerCarts);
    }

//    
//    @GetMapping("purchase_history")//TODO kato go napravi6 trqbva da ima validacii na vhodnite danni i fetch-natite entitiy-ta
//    public ResponseEntity<List<CompactPurchaseResponse>> showPurchases(@RequestParam long id) {
//
//        Customer customer = customerService.findById(id);
//
//        if (customer == null)
//            return ResponseEntity.notFound().build();
//
//        List<Purchase> purchases = purchaseService.getByCustomer(customer);
//
//        List<CompactPurchaseResponse> responses = new ArrayList<>();
//
//        for (Purchase purchase : purchases) {
//            List<PurchaseCart> purchaseCarts = purchaseCartService.getByPurchase(purchase);
//
//            if (purchaseCarts.isEmpty())
//                continue;
//
//            List<CompactProductQuantityPairResponse> pairs = new ArrayList<>();
//
//
//            for (PurchaseCart cart : purchaseCarts) {
//                CompactProductQuantityPairResponse pair = new CompactProductQuantityPairResponse();
//                pair.compactProductResponse = ProductDTOMapper
//                        .entityToCompactResponse(cart.getPurchaseCartId().getProduct());

    /// /                ProductDTOMapper.addReviewsCountToCompactResponse(pair.compactProductResponse);
//                pair.quantity = cart.getQuantity();
//
//                pairs.add(pair);
//            }
//
//            CompactPurchaseResponse compactPurchaseResponse = PurchaseMapper.purchaseDataToResponse(purchase, pairs);
//
//            responses.add(compactPurchaseResponse);
//        }
//        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responses);
//    }

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
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<CustomerResponse> getCustomerInfo() {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithActivityRefresh(userId);
        String userRole = keycloakService.getRoleByUserId(userId);

        return ResponseEntity.ok(new CustomerResponse(
                        customer.getFirstName() + " " + customer.getLastName(),
                        customer.getCustomerPfp(),
                        userRole
                )
        );
    }
}
