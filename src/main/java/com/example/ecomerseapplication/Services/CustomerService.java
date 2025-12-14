package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.CustomerAccountRequest;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.DTOs.responses.CustomerResponse;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Mappers.CustomerMapper;
import com.example.ecomerseapplication.Others.ErrorType;
import com.example.ecomerseapplication.Repositories.CustomerRepository;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PurchaseRepository purchaseRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PurchaseRepository purchaseRepository) {
        this.customerRepository = customerRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public boolean customerExists(String email) {

        return customerRepository.existsByEmail(email);
    }

//    public ResponseEntity<String> registration(CustomerAccountRequest customerAccountRequest) {
//
//        if (customerAccountRequest.email.isEmpty()
//                || customerAccountRequest.password.isEmpty()
//                || customerAccountRequest.userName.isEmpty())
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Има непопълнени полета!");
//
//        if (customerExists(customerAccountRequest.email))
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("Акаунт с този имейл вече съществува!");
//
//        if (incorrectPassword(customerAccountRequest.password.toCharArray()))
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Паролата трябва да е поне 8 символа дълга и да има поне един символ от следните: главни букви, малки букви и цифри!");
//
//        Customer customer = CustomerMapper.requestToEntity(customerAccountRequest);
//
//        customer.setCustomerPfp("deffault_pfp.jpg");
//
//        customerRepository.save(customer);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body("Регистрацията е успешна!");
//    }

    public ResponseEntity<CustomerResponse> logIn(CustomerAccountRequest customerAccountRequest) {

        if (!customerExists(customerAccountRequest.email))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (BCrypt.checkpw(customerAccountRequest.password, String.valueOf(customerRepository
                .getPassword(customerAccountRequest.email)))) {
            Customer customer = customerRepository.getCustomerByEmail(customerAccountRequest.email).orElse(null);
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            CustomerResponse customerResponse = new CustomerResponse();
            customerResponse.customerName = customer.getName();
            customerResponse.customerId = customer.getId();
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(customerResponse);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<?> addProductToFavourites(long customerId, Product product) {

        Customer customer = customerRepository.findById(customerId).orElse(null);

        if (customer == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (!customer.getFavourites().contains(product)) {

            if (customer.getFavourites().size() == 100)
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                        "Достигнат лимит на любими",
                        HttpStatus.CONFLICT.value(), "Достигнахте максималният лимит на списъка с любими!"));

            customer.getFavourites().add(product);
            customerRepository.save(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body("Успешно добавен в любими!");
        }
        else
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();

    }

    public Customer findById(long id) {
        return customerRepository.findById(id).orElse(null);
    }


    public ResponseEntity<String> removeFromFavourites(Customer customer, Product product) {

        customer.getFavourites().remove(product);

        customerRepository.save(customer);

        return ResponseEntity.status(HttpStatus.OK).body("Успешно премахнат от любими!");
    }

    public Customer getByEmail(String email) {
        return customerRepository.getByEmail(email).orElse(null);
    }

    public boolean incorrectPassword(char[] password) {
        if (password == null || password.length < 8) {
            return true;
        }

        boolean noUppercase = true;
        boolean noLowercase = true;
        boolean noDigit = true;

        for (char c : password) {
            if (Character.isUpperCase(c)) {
                noUppercase = false;
            } else if (Character.isLowerCase(c)) {
                noLowercase = false;
            } else if (Character.isDigit(c)) {
                noDigit = false;
            }
        }
        return noDigit || noLowercase || noUppercase;
    }

    public ResponseEntity<String> passwordUpdate(Customer customer, String password) {

        if (BCrypt.checkpw(password, String.valueOf(customer.getPassword())))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Новата парола не може да е същата като старата!");

        if (incorrectPassword(password.toCharArray()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Паролата трябва да е поне 8 символа дълга и да има поне един символ от следните: главни букви, малки букви и цифри!");

        customer.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)).toCharArray());

        customerRepository.save(customer);

        return ResponseEntity.ok().body("Паролата бе убновена успешно!");
    }

    public String getPfpUrl(int customerId) {
        return customerRepository.getCustomerPfp(customerId);
    }

    @Transactional
    public void removeFavoritesBatch(Customer customer, List<String> productCodes) {

        int deletedCount = 0;

        for (String productCode : productCodes) {
            customer.getFavourites().removeIf(product -> product.getProductCode().equals(productCode));
            deletedCount++;
        }

        if (deletedCount != productCodes.size())
            throw new IllegalStateException("The amount of deleted products is not equal to the amount of requested products!");



    }

    public Long getByKId(String userId) {
        return customerRepository.getIdByKeycloakId(userId);
    }
}
