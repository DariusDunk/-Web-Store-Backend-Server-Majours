package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.PurchaseRequest;
import com.example.ecomerseapplication.DTOs.responses.SuccessfulPurchaseResponse;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("purchase/")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final SessionService sessionService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService,
                              SessionService sessionService) {
        this.purchaseService = purchaseService;
        this.sessionService = sessionService;
    }

    @PostMapping("complete")
    @Transactional
    public ResponseEntity<?> createPurchase(@RequestBody PurchaseRequest request) {

        System.out.println("inside purchase controller");

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
            SuccessfulPurchaseResponse response = purchaseService.completePurchaseForCustomer(request, customer);
            System.out.println("Successful purchase response: " + response);
            return ResponseEntity.ok(response);
        }

        //todo sloji sled metoda za poukpkata da ima metod za izpra6tane na imeil

    }


}
