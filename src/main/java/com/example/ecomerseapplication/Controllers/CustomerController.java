package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Auth.helpers.UserIdExtractor;
import com.example.ecomerseapplication.DTOs.requests.*;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Validated
@RequestMapping("customer/")
public class CustomerController {

    private final CustomerService customerService;
    private final ProductService productService;
    private final PurchaseService purchaseService;
    //    private final PurchaseCartService purchaseCartService;
    private final KeycloakService keycloakService;
    private final UserIdExtractor userIdExtractor;
    private final FavoriteOfCustomerService favoriteOfCustomerService;
    private final SavedPurchaseDetailsService purchaseDetailsService;
    private final ReviewService reviewService;
    private final SessionService sessionService;

    @Autowired
    public CustomerController(CustomerService customerService,
                              ProductService productService, PurchaseService purchaseService,
                              KeycloakService keycloakService,
                              UserIdExtractor userIdExtractor,
                              FavoriteOfCustomerService favoriteOfCustomerService,
                              SavedPurchaseDetailsService purchaseDetailsService, ReviewService reviewService, SessionService sessionService) {

        this.customerService = customerService;
        this.productService = productService;
        this.purchaseService = purchaseService;

        this.keycloakService = keycloakService;
        this.userIdExtractor = userIdExtractor;
        this.favoriteOfCustomerService = favoriteOfCustomerService;
        this.purchaseDetailsService = purchaseDetailsService;
        this.reviewService = reviewService;
        this.sessionService = sessionService;
    }


    @PostMapping("favorite/add/{productCode}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> addProductToFavourites(@PathVariable @NotBlank String productCode) {

        String userId = userIdExtractor.getUserId();
        Product product = productService.findByPCode(productCode);
        Customer customer = customerService.getById(userId);

        favoriteOfCustomerService.addToFavorite(customer, product);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("favourites/p/{page}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> getFavourites(@PathVariable int page) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(userId);
        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        PageResponse<CompactProductResponse> response = favoriteOfCustomerService.getFromCustomerPaged(customer, pageRequest);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
    }


    @DeleteMapping("favourites/remove/{productCode}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeFavFromProdPage(@PathVariable String productCode) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(userId);
        Product product = productService.findByPCode(productCode);

        favoriteOfCustomerService.removeFromFavorites(customer, product);

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("favorite/remove/single")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeFromFavourites(@RequestBody @Valid RemoveOneFavRequest request) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(userId);
        Product product = productService.findByPCode(request.productCode());

        return ResponseEntity.ok(favoriteOfCustomerService.removeFromFavoritesWRefetch(
                customer,
                product,
                request.currentPage()));
    }


    @DeleteMapping("favorite/remove/batch")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> removeFromFavouritesBatch(@RequestBody @Valid RemoveFavBatchRequest request) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(userId);

        return ResponseEntity.ok(favoriteOfCustomerService.removeFavoritesBatch(
                customer,
                request.productCodes(),
                request.currentPage()));
    }

    @PostMapping("recipientTemplates/set")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> savePurchaseInformation(@RequestBody @Valid SavedRecipientDetailsRequest savedPurchaseDetailsResponse) {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(userId);
        SavedPurchaseDetails purchaseDetails = new SavedPurchaseDetails(savedPurchaseDetailsResponse, customer);

        purchaseDetailsService.saveDetails(purchaseDetails);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("recipientTemplates/get")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> getPurchaseInformation() {

        System.out.println("Inside getPurchaseInformation");

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(userId);
        SavedPurchaseDetailsResponse response = purchaseDetailsService.getByCustomer(customer);

        System.out.println("getPurchaseInformation Response: " + response);
        return ResponseEntity.ok(response);
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
        Customer customer = customerService.getById(userId);
        String userRole = keycloakService.getRoleByUserId(userId);

        return ResponseEntity.ok(new CustomerResponse(
                        customer.getFirstName() + " " + customer.getLastName(),
                        customer.getCustomerPfp(),
                        userRole
                )
        );
    }


    @GetMapping("profile")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> getFullCustomerProfileData() {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getByIdWithRelations(userId);
        int purchaseCount = purchaseService.getPurchaseCountOfCustomer(userId);
        int reviewCount = reviewService.countByCustomerId(userId);
        int favouritesCount = favoriteOfCustomerService.getFavoritesCount(customer);

        String purchaseAddress = customer.getSavedPurchaseDetails() == null
                ? "N/A"
                : customer.getSavedPurchaseDetails().getAddress();

        CustomerProfileResponse response = new CustomerProfileResponse(
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getRegistrationDate(),
                customer.getPhoneNumber(),
                purchaseAddress,
                favouritesCount,
                customer.getCustomerPfp(),
                reviewCount,
                purchaseCount

        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("update")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> updateCustomerData(@RequestBody @Valid UserDataUpdateRequest request) {
        String userId = userIdExtractor.getUserId();

        try
        {
            customerService.updateCustomerByIdFromProfile(userId, request);
        }
        catch (Exception e)
            {
            System.out.println("Error updating customer data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

        return ResponseEntity.ok().build();

    }

    @PostMapping("password-change")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> changeUserPassword() {
        Session session = sessionService.getRequestSession();
        Customer customer = session.getCustomer();

        customerService.changePassword(customer.getKeycloakId());

        return ResponseEntity.ok().build();
    }

}
