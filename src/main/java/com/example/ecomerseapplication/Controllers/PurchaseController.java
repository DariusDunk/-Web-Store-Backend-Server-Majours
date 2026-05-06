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
public class PurchaseController {//TODO sloji tezi endpoint-ove vyv spisyka za public/protected endpoints

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



    @PostMapping("complete")
    @Transactional
    public ResponseEntity<?> createPurchase(@RequestBody PurchaseRequest request) {

        Session session = sessionService.getRequestSession();

        if (session.getIsGuest()) {
            if (request.email().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(purchaseService.completePurchaseForGuest(request, session));

        }
        else
        {
            Customer customer = session.getCustomer();
            return ResponseEntity.ok(purchaseService.completePurchaseForCustomer(request, customer));
        }

        //todo sloji sled metoda za poukpkata da ima metod za izpra6tane na imeil

    }
}
