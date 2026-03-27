package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CompositeIdClasses.CustomerCartId;
import com.example.ecomerseapplication.DTOs.responses.CartItemResponse;
import com.example.ecomerseapplication.DTOs.responses.MessageResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.CustomerCart;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.CartLimitReachedException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.NoStockForCartException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.StockExceededException;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.Repositories.CustomerCartRepository;
import com.example.ecomerseapplication.enums.ResultTypes;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String  addToOrRemoveFromCart(Customer customer, Product product, Boolean doIncrement) {

        CustomerCartId cartId = new CustomerCartId(product, customer);

        CustomerCart customerCart = customerCartRepository.findById(cartId).orElse(null);

        try
        {
            if (customerCart == null) {

                if (cartsByCustomer(customer).size() >= GlobalConstants.CART_SIZE_LIMIT)
                    throw new CartLimitReachedException("Cart limit reached!");

                customerCart = new CustomerCart(cartId, (short) 1);
                customerCartRepository.save(customerCart);
                return "Успешно добавен в количката!";
            }

            if (doIncrement) {
                short quantity = customerCart.getQuantity();
                if (product.getQuantityInStock() < quantity + 1)
                    throw new StockExceededException("Stock exceeded for product " + product.getProductName() + "!");

                customerCart.setQuantity(++quantity);
                customerCartRepository.save(customerCart);
                return "Успешно увеличeно количество в количката!";
            }

            if (customerCart.getQuantity() == 1) {
                customerCartRepository.deleteById(cartId);
                return "Успешно премахнат от количката!";
            } else {
                short quantity = customerCart.getQuantity();
                customerCart.setQuantity(--quantity);

                return "Успешно намалено количество в коликчата!";
            }
        }
        catch (Exception e)
        {
            if (e instanceof CartLimitReachedException
                    || e instanceof StockExceededException) {
                throw e;
            }

            throw new RuntimeException(e);
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
    @Transactional
    public List<CartItemResponse> removeFromCartWFetch(Customer customer, String productCode) {

        int affectedRows = customerCartRepository.deleteByCustomerAndProductCode(customer, productCode);

        if (affectedRows==0)
            throw new IllegalStateException("No rows deleted");

        if (affectedRows > 1)
            throw new IllegalStateException("Multiple rows deleted unexpectedly");

        return getCartDtoByCustomer(customer);
    }

    @Transactional
    public MessageResponse addBatchToCart(Customer customer, List<Product> products) {

        boolean stockExceeded = false;

        List<CustomerCart> cart = cartsByCustomer(customer);

        int originalSize = cart.size();

        Map<CustomerCartId, CustomerCart> cartMap = Map.copyOf(cart.stream().collect(HashMap::new,
                (m, v) -> m.put(v.getCustomerCartId(), v), HashMap::putAll));

        cart = new ArrayList<>();

//        System.out.println("CART MAP size: " + cartMap.size());

        StringBuilder sb = new StringBuilder();

        int newProductsCount = 0;
        List<String> outOfStockProducts = new ArrayList<>();
        for (int i =0; i < products.size(); i++) {
            CustomerCartId newCartId = new CustomerCartId(products.get(i), customer);
            CustomerCart cartItem = cartMap.getOrDefault(newCartId, new CustomerCart(newCartId, (short)0));

            if (cartItem.getQuantity() == 0) {
                newProductsCount++;
            }

            if (cartItem.getCustomerCartId().getProduct().getQuantityInStock() < cartItem.getQuantity() + 1)
            {
                if (!stockExceeded) stockExceeded = true;

                outOfStockProducts.add(products.get(i).getProductName());
            }
            else {
                cartItem.setQuantity((short) (cartItem.getQuantity() + 1));
                cart.add(cartItem);
            }
        }

        if (stockExceeded)
        {

            sb.append("Поради недостатъчна или липсваща наличност, ");

            sb.append(outOfStockProducts.size() > 1
                    ? "продуктите: "
                    : "продуктът: ");

            for (int i = 0; i < outOfStockProducts.size(); i++) {
                String separator = switch (outOfStockProducts.size() - (i+1)) {
                    case 0 -> "";
                    case 1 -> " и ";
                    default -> ", ";
                };
                sb.append(outOfStockProducts.get(i)).append(separator);
            }

            if (outOfStockProducts.size() > 1)
                sb.append(" не бяха добавени или увеличени в количката!");
            else sb.append(" не беше добавен или увеличен в количката!");
        }

        if (newProductsCount + originalSize > GlobalConstants.CART_SIZE_LIMIT) {
            throw new CartLimitReachedException("Cart limit reached!");
        }

        try {
            int savedSize = customerCartRepository.saveAll(cart).size();

            if (stockExceeded) {
                if (savedSize == 0)
                    throw new NoStockForCartException(sb.toString());

                return new MessageResponse(ResultTypes.PARTIAL_SUCCESS.getValue(), "Не всички продукти бяха добавени", sb.toString());
            }

            return new MessageResponse(ResultTypes.SUCCESS.getValue(), "", "Успешно добавени в количката!");

        } catch (Exception e) {
            if (e instanceof NoStockForCartException) {
                throw e;
            }

            throw new RuntimeException(e);
        }

    }

    @Transactional
    public List<CartItemResponse> removeBatchFromCartWFetch(Customer customer, List<String> productCodes) {

        int deletedCount = customerCartRepository.deleteBatchByCustomerAndPCodes(customer, productCodes);

        if (deletedCount != productCodes.size()|| deletedCount == 0) {
            throw new IllegalArgumentException("Not all products were found in the cart!");
        }

      return customerCartRepository.findDtoByCustomer(customer.getKeycloakId());
    }
}
