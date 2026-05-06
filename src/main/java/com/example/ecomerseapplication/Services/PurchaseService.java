package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CompositeIdClasses.PurchaseCartId;
import com.example.ecomerseapplication.DTOs.requests.ProductQuantityForCartRequest;
import com.example.ecomerseapplication.DTOs.requests.PurchaseRequest;
import com.example.ecomerseapplication.DTOs.requests.RecipientDataRequest;
import com.example.ecomerseapplication.DTOs.responses.SuccessfulPurchaseResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.PurchaseProductDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.TotalsDTO;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.PessimisticLockOrTimeoutPurchaseException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.StockForNamedProductExceeded;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Mappers.PurchaseMapper;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import com.example.ecomerseapplication.enums.PaymentMethod;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseCartService purchaseCartService;
    private final ProductService productService;
    private final CartProductService cartProductService;

    @Autowired
    public PurchaseService(PurchaseRepository purchaseRepository, PurchaseCartService purchaseCartService, ProductService productService, CartProductService cartProductService) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseCartService = purchaseCartService;
        this.productService = productService;
        this.cartProductService = cartProductService;
    }

    public Purchase save(Purchase purchase) {
        return purchaseRepository.save(purchase);
    }

    @Transactional
    public SuccessfulPurchaseResponse completePurchase(PurchaseRequest request, Customer customer) {

        List<ProductQuantityForCartRequest> productPairs = request.products();
        List<String> productCodes = productPairs.stream().map(ProductQuantityForCartRequest::productCode).toList();
        List<Product> productsForPurchase = getLockedProductsForPurchase(productCodes);
        RecipientDataRequest recipientData = request.recipientData();
        PaymentMethod paymentMethod = request.paymentMethod();
        String purchaseCode = generateCode(LocalDateTime.now());
        Map<String, Product> productByCodeMap = Map
                .copyOf(productsForPurchase
                        .stream()
                        .collect(HashMap::new,
                (m, v) -> m.put(v.getProductCode(), v), HashMap::putAll)
                );

        validateStock(productPairs, productByCodeMap);

        Map<String, PurchaseProductDTO> purchaseProductMap = buildPurchaseItems(productPairs, productByCodeMap);
        TotalsDTO totals = calculateTotals(purchaseProductMap);
        Purchase purchase = createAuthPurchase(customer, totals, recipientData, purchaseCode, paymentMethod);

        savePurchaseItems(productsForPurchase, purchase, purchaseProductMap);
        cartCleanup(request, customer, productCodes);

        return PurchaseMapper.entityToSuccessResponse(purchase);
    }

    private void cartCleanup(PurchaseRequest request, Customer customer, List<String> productCodes) {
        if (!request.isDirectPurchase())
        {
            cartProductService.removeBatchFromCart(customer, productCodes);
        }
    }

    private void savePurchaseItems(List<Product> productsForPurchase, Purchase purchase, Map<String, PurchaseProductDTO> purchaseProductMap) {
        List<PurchaseCart> purchaseCarts = new ArrayList<>();

        for (Product currectProduct: productsForPurchase)
        {
            PurchaseCartId purchaseCartId = new PurchaseCartId();
            purchaseCartId.setPurchase(purchase);
            purchaseCartId.setProduct(currectProduct);
            short quantity = purchaseProductMap.get(currectProduct.getProductCode()).quantity();
            int finalPrice = purchaseProductMap.get(currectProduct.getProductCode()).finalPrice();

            purchaseCarts.add(new PurchaseCart(purchaseCartId,
                    quantity,finalPrice));

        }

        purchaseCartService.saveCarts(purchaseCarts);
    }

    private Purchase createAuthPurchase(Customer customer, TotalsDTO totals, RecipientDataRequest recipientData, String purchaseCode, PaymentMethod paymentMethod) {
        Purchase purchase = new Purchase(customer,
                totals.totalCost(),
                recipientData.contactName(),
                recipientData.contactNumber(),
                recipientData.address(),
                purchaseCode,
                totals.shippingFee(),
                totals.productTotal(),
                paymentMethod);

       return save(purchase);
    }

    private static TotalsDTO calculateTotals(Map<String, PurchaseProductDTO> purchaseProductMap) {
       int productTotal = purchaseProductMap.values().stream().mapToInt(PurchaseProductDTO::finalPrice).sum();
        int freeShippingThresholdCents = 6000;
        int shippingFee = productTotal >= freeShippingThresholdCents ? 0 : 100;
        int totalCost = productTotal + shippingFee;

        return new TotalsDTO(productTotal, shippingFee, totalCost);
    }

    @NonNull
    private static Map<String, PurchaseProductDTO> buildPurchaseItems(List<ProductQuantityForCartRequest> productPairs, Map<String, Product> productByCodeMap) {
        Map<String, PurchaseProductDTO> purchaseProductMap = new HashMap<>();

        for (ProductQuantityForCartRequest productPair: productPairs) {
            Product product = productByCodeMap.get(productPair.productCode());

            SaleProduct saleProduct = product.getMainSaleProduct().orElse(null);

            int finalPrice =  ProductDTOMapper.calculatePriceForDto(saleProduct, product.getOriginalPriceStotinki());

            purchaseProductMap.put(productPair.productCode(), new PurchaseProductDTO(product, productPair.quantity(), finalPrice));
            product.setQuantityInStock(product.getQuantityInStock() - productPair.quantity());
        }
        return purchaseProductMap;
    }

    private static void validateStock(List<ProductQuantityForCartRequest> productPairs, Map<String, Product> productByCodeMap) {
        for (ProductQuantityForCartRequest productPair: productPairs) {
            Product product = productByCodeMap.get(productPair.productCode());
            if (product == null)
                throw new ResourceNotFoundException("Product not found!");

            if (product.getQuantityInStock() < productPair.quantity())
                throw new StockForNamedProductExceeded("Stock exceeded for product during purchase",
                        product.getProductName(),
                        product.getQuantityInStock());
            
        }
    }

    private List<Product> getLockedProductsForPurchase(List<String> productCodes) {
        List<Product> productsForPurchase;
        try {
            productsForPurchase = productService.getByCodesForPurchaseWithLocking(productCodes);
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new PessimisticLockOrTimeoutPurchaseException("Pessimistic lock or timeout exception occurred during purchase");
        }
        return productsForPurchase;
    }


    public List<Purchase> getByCustomer(Customer customer) {
        return purchaseRepository.getByCustomer(customer.getKeycloakId());
    }

    public static String generateCode(LocalDateTime timeStamp)  {
        int year = timeStamp.getYear();
        int month = timeStamp.getMonthValue();
        int day = timeStamp.getDayOfMonth();
        int hour = timeStamp.getHour();
        int minute =timeStamp.getMinute();

        String randomDigits = new Random().ints(3, 1, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());

        return String.format("%d%02d%s%02d",hour, month*7+minute+ day*3,randomDigits,year);
    }
}
