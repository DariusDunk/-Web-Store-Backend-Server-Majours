package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CompositeIdClasses.CustomerCartId;
import com.example.ecomerseapplication.DTOs.responses.CartItemResponse;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.CustomerCart;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.Repositories.CustomerCartRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerCartService {


    private final CustomerCartRepository customerCartRepository;

    @Autowired
    public CustomerCartService(CustomerCartRepository customerCartRepository) {
        this.customerCartRepository = customerCartRepository;
    }

    @Transactional
    public ResponseEntity<?> addToOrRemoveFromCart(Customer customer, Product product, Boolean doIncrement) {

        CustomerCartId cartId = new CustomerCartId(product, customer);

        CustomerCart customerCart = customerCartRepository.findById(cartId).orElse(null);

        if (customerCart == null) {

            if (cartsByCustomer(customer).size() >= GlobalConstants.cartSizeLimit)
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                        "Неуспешно добавяне на продукт",
                        HttpStatus.CONFLICT.value(),
                        "Достигнахте лимита на количката"));


            customerCart = new CustomerCart(cartId, (short)1);
            customerCartRepository.save(customerCart);
            return ResponseEntity.status(HttpStatus.CREATED).body("Успешно добавен в количката!");
        }

        if (doIncrement)
        {
            short quantity = customerCart.getQuantity();
            if (product.getQuantityInStock() < quantity + 1)
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.DEMAND_EXCEEDS_SUPPLY,
                        "Неуспешно увеличение на бройка",
                        HttpStatus.CONFLICT.value(),
                        "Изисканото количество надхвърля наличното за този продуктит, той не бе добавен или увеличен в количката "));


            customerCart.setQuantity(++quantity);
            customerCartRepository.save(customerCart);
            return ResponseEntity.ok("Успешно увелично количество в количката!");
        }

        else

        if (customerCart.getQuantity() == 1) {
            customerCartRepository.deleteById(cartId);
            return ResponseEntity.status(HttpStatus.OK).body("Успешно премахнат от количката!");
        }
        else {
            short quantity = customerCart.getQuantity();
            customerCart.setQuantity(--quantity);

            return ResponseEntity.ok("Успешно намалено количество в коликчата!");
        }

    }

    public boolean cartExists(Customer customer, Product product) {
        return customerCartRepository.existsByCustomerCartId(new CustomerCartId(product, customer));
    }

    public List<CustomerCart> cartsByCustomer(Customer customer) {
        return customerCartRepository.findByCustomer(customer.getKeycloakId());
    }

    public List<CartItemResponse> getCartDtoByCustomer(Customer customer) {
        return customerCartRepository.findDtoByCustomer(customer.getKeycloakId());
    }

    public void removeFromCart(Customer customer, String productCode) {
        List<CustomerCart> cart = cartsByCustomer(customer);

        if (cart.isEmpty()) throw new IllegalArgumentException("Cart is empty!");

        CustomerCart cartItem = cart.stream().filter(c -> c.getCustomerCartId().getProduct().getProductCode().equals(productCode)).findFirst().orElseThrow();

        customerCartRepository.delete(cartItem);

    }

    @Transactional
    public ResponseEntity<?> addBatchToCart(Customer customer, List<Product> products) {

        boolean stockExceeded = false;

        List<CustomerCart> cart = cartsByCustomer(customer);

        int originalSize = cart.size();

        Map<CustomerCartId, CustomerCart> cartMap = Map.copyOf(cart.stream().collect(HashMap::new,
                (m, v) -> m.put(v.getCustomerCartId(), v), HashMap::putAll));

        cart = new ArrayList<>();

        System.out.println("CART MAP size: " + cartMap.size());

        StringBuilder sb = new StringBuilder("Количеството в наличност на продуктите: ");

        int newProductsCount = 0;

        for (Product product : products) {
            CustomerCartId cartId = new CustomerCartId(product, customer);
            CustomerCart cartItem = cartMap.getOrDefault(cartId, new CustomerCart(cartId, (short)0));

            if (cartItem.getQuantity() == 0) {
                newProductsCount++;
            }

            if (cartItem.getCustomerCartId().getProduct().getQuantityInStock() < cartItem.getQuantity() + 1)
            {
                if (!stockExceeded) stockExceeded = true;

                sb.append(product.getProductName()).append(", ");
            }
            else {
                cartItem.setQuantity((short) (cartItem.getQuantity() + 1));
                cart.add(cartItem);
            }
        }

        if (stockExceeded)
        {
            sb.replace(sb.length() - 2, sb.length(), " ");
            sb.append("е по-малко от изискваното, те не бяха добавени или с увеличено количество в количката");
        }

        if (newProductsCount + originalSize > GlobalConstants.cartSizeLimit) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                    "Неуспешно добавяне на продукт",
                    HttpStatus.CONFLICT.value(),
                    "Достигнахте лимита на количката"));
        }

        int savedSize = customerCartRepository.saveAll(cart).size();

        if (stockExceeded && savedSize == 0) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.NO_DATA_FOR_QUERY,
                    "Продуктите не бяха добавени!",
                    HttpStatus.CONFLICT.value()
                    , sb.toString()));
        }

        return stockExceeded? ResponseEntity
                .status(HttpStatus.MULTI_STATUS)
                .body(new ErrorResponse(ErrorType.DEMAND_EXCEEDS_SUPPLY,"Не всички продукти бяха добавени",HttpStatus.MULTI_STATUS.value(),sb.toString()))
                : ResponseEntity.ok("Успешно добавени в количката!");

    }

    @Transactional
    public ResponseEntity<?> removeBatchFromCart(Customer customer, List<String> productCodes) {

        List<CustomerCart> cartContent = cartsByCustomer(customer);

        List<CustomerCart> entriesForDeletion = new ArrayList<>();

        for (String productCode : productCodes) {
            cartContent.stream().filter(c -> c
                    .getCustomerCartId()
                    .getProduct()
                    .getProductCode()
                    .equals(productCode))
                    .findFirst()
                    .ifPresent(entriesForDeletion::add);
        }

        if (entriesForDeletion.size() != productCodes.size()) {
            throw new IllegalArgumentException("Not all products were found in the cart!");
        }

        customerCartRepository.deleteAll(entriesForDeletion);

        return ResponseEntity.ok().build();

    }
}
