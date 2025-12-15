package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.CompositeIdClasses.CustomerCartId;
import com.example.ecomerseapplication.CompositeIdClasses.PurchaseCartId;
import com.example.ecomerseapplication.DTOs.requests.PurchaseRequest;
import com.example.ecomerseapplication.DTOs.requests.SavedRecipientDetailsRequest;
import com.example.ecomerseapplication.DTOs.responses.CompactProductQuantityPairResponse;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.DTOs.responses.PurchaseResponse;
import com.example.ecomerseapplication.DTOs.responses.SavedPurchaseDetailsResponse;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Mappers.PurchaseMapper;
import com.example.ecomerseapplication.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("purchase/")
public class PurchaseController {

    private final PurchaseService purchaseService;

    private final SavedPurchaseDetailsService purchaseDetailsService;

    private final CustomerService customerService;

    private final CustomerCartService customerCartService;

    private final PurchaseCartService purchaseCartService;
    private final ProductService productService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService, SavedPurchaseDetailsService purchaseDetailsService, CustomerService customerService, CustomerCartService customerCartService, PurchaseCartService purchaseCartService, ProductService productService) {
        this.purchaseService = purchaseService;
        this.purchaseDetailsService = purchaseDetailsService;
        this.customerService = customerService;
        this.customerCartService = customerCartService;
        this.purchaseCartService = purchaseCartService;
        this.productService = productService;
    }

    @PostMapping("savedetails")
    @Transactional
    public ResponseEntity<String> savePurchaseInformation(@RequestBody SavedRecipientDetailsRequest savedPurchaseDetailsResponse,
                                                          long id) {
        Customer customer = customerService.findById(id);

        if (customer == null)
            return ResponseEntity.notFound().build();

        SavedPurchaseDetails purchaseDetails = new SavedPurchaseDetails(savedPurchaseDetailsResponse, customer);

        return purchaseDetailsService.saveDetails(purchaseDetails);
    }

    @GetMapping("recipientTemplates/get")
    public ResponseEntity<?> getPurchaseInformation(long id) {
        Customer customer = customerService.findById(id);

        if (customer == null)
            return ResponseEntity.notFound().build();

        return purchaseDetailsService.getByCustomer(customer);
    }

//    @PostMapping("complete") TODO opravi
//    @Transactional
//    public ResponseEntity<PurchaseResponse> createPurchase(@RequestBody PurchaseRequest purchaseRequest) {

//        Customer customer = customerService.findById(purchaseRequest.customerId);
//
//        if (customer==null)
//          return  ResponseEntity.notFound().build();
//
//        if (purchaseRequest.savedRecipientDetailsRequest == null) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        Purchase purchase = PurchaseMapper.requestToEntity(purchaseRequest.savedRecipientDetailsRequest);
//
//        purchase.setCustomer(customer);
//
//        List<CustomerCart> customerCarts = customerCartService.cartsByCustomer(customer);
//
//        List<Product> updatedQuantProducts = new ArrayList<>();
//
//        if (customerCarts.isEmpty())
//            return ResponseEntity.notFound().build();
//
//        for (CustomerCart cart : customerCarts) {
//            Product currectProduct = cart.getCustomerCartId().getProduct();
//            if (currectProduct.getQuantityInStock() < cart.getQuantity()) {
//                return ResponseEntity.badRequest().build();//TODO tuk kato se vru6ta custom response, trqbva da kazva to4no koi produkt nqma broiki
//            }
//
//            currectProduct.setQuantityInStock(currectProduct.getQuantityInStock() - cart.getQuantity());
//
//            updatedQuantProducts.add(currectProduct);
//
//        }
//
//        productService.saveAll(updatedQuantProducts);
//
//
//        int totalCost = 0;
//
//        for (CustomerCart customerCart:customerCarts) {
//            totalCost+=customerCart
//                    .getCustomerCartId()
//                    .getProduct()
//                    .getSalePriceStotinki()*customerCart.getQuantity();
//        }
//
//        purchase.setTotalCost(totalCost);
//
//        Purchase managedPurchase = purchaseService.save(purchase);
//
//        PurchaseCartId purchaseCartId;
//
//        List<PurchaseCart> purchaseCarts = new ArrayList<>();
//
//        for (CustomerCart customerCart:customerCarts)
//        {
//            purchaseCartId = new PurchaseCartId();
//            purchaseCartId.setPurchase(managedPurchase);
//            purchaseCartId.setProduct(customerCart
//                    .getCustomerCartId()
//                    .getProduct());
//
//            purchaseCarts.add(new PurchaseCart(purchaseCartId,
//                    customerCart.getQuantity()));
//        }
//
//        purchaseCartService.saveCarts(purchaseCarts);
//
//        PurchaseResponse purchaseResponse = PurchaseMapper.entityToResponse(purchase);
//
//        for (CustomerCart customerCart:customerCarts) {
//
//            CompactProductResponse compactProduct = ProductDTOMapper
//                    .entityToCompactResponse(customerCart.getCustomerCartId().getProduct());
//
//            CompactProductQuantityPairResponse pair = new CompactProductQuantityPairResponse();
//            pair.compactProductResponse = compactProduct;
//            pair.quantity = customerCart.getQuantity();
//
//            purchaseResponse.productQuantityPairs.add(pair);
//        }
//
//        customerCartService.clearCart(customer);

//        return purchaseService.completePurchase(purchaseRequest);
//    }
}
