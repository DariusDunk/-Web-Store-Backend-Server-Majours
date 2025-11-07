package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CompositeIdClasses.PurchaseCartId;
import com.example.ecomerseapplication.DTOs.requests.PurchaseRequest;
import com.example.ecomerseapplication.DTOs.responses.CompactProductQuantityPairResponse;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.DTOs.responses.PurchaseResponse;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Mappers.PurchaseMapper;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final CustomerService customerService;
    private final CustomerCartService customerCartService;
    private final ProductService productService;
    private final PurchaseCartService purchaseCartService;

    @Autowired
    public PurchaseService(PurchaseRepository purchaseRepository, CustomerService customerService, CustomerCartService customerCartService, ProductService productService, PurchaseCartService purchaseCartService) {
        this.purchaseRepository = purchaseRepository;
        this.customerService = customerService;
        this.customerCartService = customerCartService;
        this.productService = productService;
        this.purchaseCartService = purchaseCartService;
    }

    public Purchase save(Purchase purchase) {
        return purchaseRepository.save(purchase);
    }
    @Transactional
    public ResponseEntity<PurchaseResponse> completePurchase(PurchaseRequest purchaseRequest) {

        Customer customer = customerService.findById(purchaseRequest.customerId);

        if (customer==null)
            return  ResponseEntity.notFound().build();

        if (purchaseRequest.savedRecipientDetailsRequest == null) {
            return ResponseEntity.badRequest().build();
        }

        Purchase purchase = PurchaseMapper.requestToEntity(purchaseRequest.savedRecipientDetailsRequest);

        purchase.setCustomer(customer);

        List<CustomerCart> customerCarts = customerCartService.cartsByCustomer(customer);

        List<Product> updatedQuantProducts = new ArrayList<>();

        if (customerCarts.isEmpty())
            return ResponseEntity.notFound().build();

        for (CustomerCart cart : customerCarts) {
            Product currectProduct = cart.getCustomerCartId().getProduct();
            if (currectProduct.getQuantityInStock() < cart.getQuantity()) {
                return ResponseEntity.badRequest().build();//TODO tuk kato se vru6ta custom response, trqbva da kazva to4no koi produkt nqma broiki
            }

            currectProduct.setQuantityInStock(currectProduct.getQuantityInStock() - cart.getQuantity());

            updatedQuantProducts.add(currectProduct);

        }

//        productService.saveAll(updatedQuantProducts);


        int totalCost = 0;

        for (CustomerCart customerCart:customerCarts) {
            totalCost+=customerCart
                    .getCustomerCartId()
                    .getProduct()
                    .getSalePriceStotinki()*customerCart.getQuantity();
        }

        purchase.setTotalCost(totalCost);

        Purchase managedPurchase = save(purchase);

        PurchaseCartId purchaseCartId;

        List<PurchaseCart> purchaseCarts = new ArrayList<>();

        for (CustomerCart customerCart:customerCarts)
        {

            Product currectProduct = customerCart.getCustomerCartId().getProduct();

            purchaseCartId = new PurchaseCartId();
            purchaseCartId.setPurchase(managedPurchase);
            purchaseCartId.setProduct(currectProduct);

            purchaseCarts.add(new PurchaseCart(purchaseCartId,
                    customerCart.getQuantity(),currectProduct.getSalePriceStotinki()));//TODO tova moje da se promeni i da vzema druga cena

        }

        purchaseCartService.saveCarts(purchaseCarts);

        PurchaseResponse purchaseResponse = PurchaseMapper.entityToResponse(purchase);

        for (CustomerCart customerCart:customerCarts) {

            CompactProductResponse compactProduct = ProductDTOMapper
                    .entityToCompactResponse(customerCart.getCustomerCartId().getProduct());

            CompactProductQuantityPairResponse pair = new CompactProductQuantityPairResponse();
            pair.compactProductResponse = compactProduct;
            pair.quantity = customerCart.getQuantity();

            purchaseResponse.productQuantityPairs.add(pair);
        }

        customerCartService.clearCart(customer);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(purchaseResponse);
    }


    public List<Purchase> getByCustomer(Customer customer) {
        return purchaseRepository.getByCustomer(customer);
    }
}
