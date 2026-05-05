package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Auth.helpers.UserIdExtractor;
import com.example.ecomerseapplication.DTOs.requests.PurchaseRequest;
import com.example.ecomerseapplication.DTOs.requests.SavedRecipientDetailsRequest;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Services.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("purchase/")
public class PurchaseController {//TODO kogato stigne6 tuk premahni vsqkakvo izpolzvane na http response entity-ta vyv service klasovete i slagai custom exceptions

    private final PurchaseService purchaseService;

    private final SavedPurchaseDetailsService purchaseDetailsService;

    private final CustomerService customerService;

    private final UserIdExtractor userIdExtractor;
    private final SessionService sessionService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService,
                              SavedPurchaseDetailsService purchaseDetailsService,
                              CustomerService customerService,
                              UserIdExtractor userIdExtractor,
                              SessionService sessionService) {
        this.purchaseService = purchaseService;
        this.purchaseDetailsService = purchaseDetailsService;
        this.customerService = customerService;
        this.userIdExtractor = userIdExtractor;
        this.sessionService = sessionService;
    }

    @PostMapping("savedetails")
    @Transactional
    public ResponseEntity<String> savePurchaseInformation(@RequestBody @Valid SavedRecipientDetailsRequest savedPurchaseDetailsResponse) {

        String userId = userIdExtractor.getUserId();

        Customer customer = customerService.getById(userId);

        SavedPurchaseDetails purchaseDetails = new SavedPurchaseDetails(savedPurchaseDetailsResponse, customer);

        return purchaseDetailsService.saveDetails(purchaseDetails);
    }

    @GetMapping("recipientTemplates/get")
    public ResponseEntity<?> getPurchaseInformation() {

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(userId);

        return purchaseDetailsService.getByCustomer(customer);
    }

    @PostMapping("complete")
    @Transactional
    public ResponseEntity<?> createPurchase(@RequestBody PurchaseRequest request) {

        Session session = sessionService.getRequestSession();

        if (session.getIsGuest()) {
            if (request.email().isBlank()) {
                return ResponseEntity.badRequest().build();
            }
        }
        else
        {
            Customer customer = session.getCustomer();
            return ResponseEntity.ok(purchaseService.completePurchase(request, customer));
        }

        //todo sloji sled metoda za poukpkata da ima metod za izpra6tane na imeil

        return ResponseEntity.badRequest().build();
    }
}
