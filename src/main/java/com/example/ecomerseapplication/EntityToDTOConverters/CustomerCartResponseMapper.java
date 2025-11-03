package com.example.ecomerseapplication.EntityToDTOConverters;

import com.example.ecomerseapplication.DTOs.responses.CompactProductQuantityPairResponse;
import com.example.ecomerseapplication.DTOs.responses.CustomerCartResponse;
import com.example.ecomerseapplication.Entities.CustomerCart;
import com.example.ecomerseapplication.Entities.Product;
import java.util.List;

public class CustomerCartResponseMapper {

    public static CustomerCartResponse listToResponse(List<CustomerCart> customerCarts) {

        CustomerCartResponse customerCartResponse = new CustomerCartResponse();
        int totalCost = 0;

        for (CustomerCart customerCart : customerCarts) {
            Product product = customerCart.getCustomerCartId().getProduct();

            CompactProductQuantityPairResponse compactProductQuantityPair = new CompactProductQuantityPairResponse();
            compactProductQuantityPair.compactProductResponse = ProductDTOMapper.entityToCompactResponse(product);
            compactProductQuantityPair.quantity = customerCart.getQuantity();

            totalCost += product.getSalePriceStotinki() * customerCart.getQuantity();

            customerCartResponse.productQuantityPair.add(compactProductQuantityPair);
        }
        customerCartResponse.totalCost = totalCost;

        return customerCartResponse;
    }
}
