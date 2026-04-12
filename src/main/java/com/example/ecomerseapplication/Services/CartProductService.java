package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CompositeIdClasses.CustomerCartId;
import com.example.ecomerseapplication.DTOs.responses.CartItemResponse;
import com.example.ecomerseapplication.DTOs.responses.CartSummaryResponse;
import com.example.ecomerseapplication.DTOs.responses.MessageResponse;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.CartLimitReachedException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.NoStockForCartException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.StockExceededException;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.Repositories.CartProductRepository;
import com.example.ecomerseapplication.enums.ResultTypes;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartProductService {


    private final CartProductRepository cartProductRepository;
    private final CartService cartService;

    @Autowired
    public CartProductService(CartProductRepository cartProductRepository, CartService cartService) {
        this.cartProductRepository = cartProductRepository;
        this.cartService = cartService;
    }

    @Transactional
    public String  addToOrRemoveFromCart(Customer customer, Product product, Boolean doIncrement) {

        Cart cart = cartService.getOrCreateByCustomer(customer);

        CustomerCartId cartId = new CustomerCartId(product, cart);

        CartProduct cartProduct = cartProductRepository.findById(cartId).orElse(null);

        try
        {
            if (cartProduct == null) {

                if (cartsByCustomer(customer).size() >= GlobalConstants.CART_SIZE_LIMIT)
                    throw new CartLimitReachedException("Cart limit reached!");

                cartProduct = new CartProduct(cartId, (short) 1);
                cartProductRepository.save(cartProduct);
                return "Успешно добавен в количката!";
            }

            return doIncrementMainLogic(product, doIncrement, cartId, cartProduct);
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

    @NonNull
    private String doIncrementMainLogic(Product product, Boolean doIncrement, CustomerCartId cartId, CartProduct cartProduct) {
        if (doIncrement) {
            short quantity = cartProduct.getQuantity();
            if (product.getQuantityInStock() < quantity + 1)
                throw new StockExceededException("Stock exceeded for product " + product.getProductName() + "!");

            cartProduct.setQuantity(++quantity);
            cartProductRepository.save(cartProduct);
            return "Успешно увеличeно количество в количката!";
        }

        if (cartProduct.getQuantity() == 1) {
            cartProductRepository.deleteById(cartId);
            return "Успешно премахнат от количката!";
        } else {
            short quantity = cartProduct.getQuantity();
            cartProduct.setQuantity(--quantity);

            return "Успешно намалено количество в коликчата!";
        }
    }

    //TODO TEST
    @Transactional
    public String  addToOrRemoveFromCart(Session session, Product product, Boolean doIncrement) {

        Cart cart = cartService.getOrCreateBySession(session);

        CustomerCartId cartId = new CustomerCartId(product, cart);

        CartProduct cartProduct = cartProductRepository.findById(cartId).orElse(null);

        try
        {
            if (cartProduct == null) {

                if (cartsBySession(session).size() >= GlobalConstants.CART_SIZE_LIMIT)
                    throw new CartLimitReachedException("Cart limit reached!");

                cartProduct = new CartProduct(cartId, (short) 1);
                cartProductRepository.save(cartProduct);
                return "Успешно добавен в количката!";
            }

            return doIncrementMainLogic(product, doIncrement, cartId, cartProduct);
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

    public boolean cartItemExistsByCustomer(Customer customer, Product product) {
        Cart cart = cartService.getOrCreateByCustomer(customer);
        return cartProductRepository.existsByCustomerCartId(new CustomerCartId(product, cart));
    }
    //TODO TEST
    public boolean cartItemExistsBySession(Session session, Product product) {
        Cart cart = cartService.getOrCreateBySession(session);
        return cartProductRepository.existsByCustomerCartId(new CustomerCartId(product, cart));
    }

    public List<CartProduct> cartsByCustomer(Customer customer) {
        return cartProductRepository.findByCustomer(customer.getKeycloakId());
    }
    //TODO TEST
    public List<CartProduct> cartsBySession(Session session) {
        return cartProductRepository.getBySession(session);
    }

    public List<CartItemResponse> getCartDtoByCustomer(Customer customer) {
        return cartProductRepository.findDtoByCustomer(customer.getKeycloakId());
    }
    //TODO TEST
    public List<CartItemResponse> getCartDtoBySession(Session session) {
        return cartProductRepository.findDtoBySession(session);
    }

    @Transactional
    public List<CartItemResponse> removeFromCartWFetch(Customer customer, String productCode) {
        Cart cart = cartService.getOrCreateByCustomer(customer);
        int affectedRows = cartProductRepository.deleteByCartAndProductCode(cart, productCode);

        if (affectedRows==0)
            throw new IllegalStateException("No rows deleted");

        if (affectedRows > 1)
            throw new IllegalStateException("Multiple rows deleted unexpectedly");

        return getCartDtoByCustomer(customer);
    }

    //TODO TEST
    @Transactional
    public List<CartItemResponse> removeFromCartWFetch(Session session, String productCode) {
        Cart cart = cartService.getOrCreateBySession(session);
        int affectedRows = cartProductRepository.deleteByCartAndProductCode(cart, productCode);

        if (affectedRows==0)
            throw new IllegalStateException("No rows deleted");

        if (affectedRows > 1)
            throw new IllegalStateException("Multiple rows deleted unexpectedly");

        return getCartDtoBySession(session);
    }

    @Transactional
    public MessageResponse addBatchToCart(Customer customer, List<Product> products) {

        boolean stockExceeded = false;
        Cart cart = cartService.getOrCreateByCustomer(customer);
        List<CartProduct> cartProducts = cartsByCustomer(customer);
        return cartBatchAddLogic(products, cartProducts, cart, stockExceeded);

    }

    //TODO TEST
    @Transactional
    public MessageResponse addBatchToCart(Session session, List<Product> products) {

        boolean stockExceeded = false;
        Cart cart = cartService.getOrCreateBySession(session);
        List<CartProduct> cartProducts = cartsBySession(session);
        return cartBatchAddLogic(products, cartProducts, cart, stockExceeded);

    }

    @NonNull
    private MessageResponse cartBatchAddLogic(List<Product> products, List<CartProduct> cartProducts, Cart cart, boolean stockExceeded) {
        int originalSize = cartProducts.size();
        Map<CustomerCartId, CartProduct> cartMap = Map.copyOf(cartProducts.stream().collect(HashMap::new,
                (m, v) -> m.put(v.getCustomerCartId(), v), HashMap::putAll));

        cartProducts = new ArrayList<>();

//        System.out.println("CART MAP size: " + cartMap.size());

        StringBuilder sb = new StringBuilder();

        int newProductsCount = 0;
        List<String> outOfStockProducts = new ArrayList<>();
        for (Product product : products) {
            CustomerCartId newCartId = new CustomerCartId(product, cart);
            CartProduct cartItem = cartMap.getOrDefault(newCartId, new CartProduct(newCartId, (short) 0));

            if (cartItem.getQuantity() == 0) {
                newProductsCount++;
            }

            if (cartItem.getCustomerCartId().getProduct().getQuantityInStock() < cartItem.getQuantity() + 1) {
                if (!stockExceeded) stockExceeded = true;

                outOfStockProducts.add(product.getProductName());
            } else {
                cartItem.setQuantity((short) (cartItem.getQuantity() + 1));
                cartProducts.add(cartItem);
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
            int savedSize = cartProductRepository.saveAll(cartProducts).size();

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

        Cart cart = cartService.getOrCreateByCustomer(customer);
        int deletedCount = cartProductRepository.deleteBatchByCartAndPCodes(cart, productCodes);

        if (deletedCount != productCodes.size()|| deletedCount == 0) {
            throw new IllegalArgumentException("Not all products were found in the cart!");
        }

      return cartProductRepository.findDtoByCustomer(customer.getKeycloakId());
    }

    //TODO TEST
    @Transactional
    public List<CartItemResponse> removeBatchFromCartWFetch(Session session, List<String> productCodes) {

        Cart cart = cartService.getOrCreateBySession(session);
        int deletedCount = cartProductRepository.deleteBatchByCartAndPCodes(cart, productCodes);

        if (deletedCount != productCodes.size()|| deletedCount == 0) {
            throw new IllegalArgumentException("Not all products were found in the cart!");
        }

        return cartProductRepository.findDtoBySession(session);
    }

    public String addQuantityToCart(Product product, short quantity, Customer customer) {

        try
        {
            Cart cart = cartService.getOrCreateByCustomer(customer);
            CustomerCartId cartId = new CustomerCartId(product, cart);
            CartProduct cartProduct = cartProductRepository.findById(cartId).orElse(null);
            return addQuantityToCartMain(product, quantity, cartId, cartProduct);
        }
        catch (Exception e)
        {
            if (e instanceof StockExceededException)
                throw e;
            else
                throw new RuntimeException("-----------------Quantity cart addition for auth user failed-----------------\n " + e.getMessage());

        }
    }

    //TODO TEST
    public String addQuantityToCart(Product product, short quantity, Session session) {

        try
        {
            Cart cart = cartService.getOrCreateBySession(session);
            CustomerCartId cartId = new CustomerCartId(product, cart);
            CartProduct cartProduct = cartProductRepository.findById(cartId).orElse(null);
            return addQuantityToCartMain(product, quantity, cartId, cartProduct);
        }
        catch (Exception e)
        {
            if (e instanceof StockExceededException)
                throw e;
            else
                throw new RuntimeException("-----------------Quantity cart addition for session failed-----------------\n " + e.getMessage());

        }
    }

    @NonNull
    private String addQuantityToCartMain(Product product, short quantity, CustomerCartId cartId, CartProduct cartProduct) {
        int quantityInStock = product.getQuantityInStock();

        if (cartProduct != null) {
            short currentCartQuantity = cartProduct.getQuantity();
            if (quantityInStock >= currentCartQuantity + quantity) {
                cartProduct.setQuantity((short) (currentCartQuantity + quantity));
                cartProductRepository.save(cartProduct);
                return "Успешно увеличен в количката!";
            }
            else
                throw new StockExceededException("Stock exceeded for product " + product.getProductName() + "!");
        } else {
            if (quantityInStock < quantity)
                throw new StockExceededException("Stock exceeded for product " + product.getProductName() + "!");
            cartProduct = new CartProduct(cartId, quantity);
            cartProductRepository.save(cartProduct);
            return "Успешно добавен в количката!";

        }
    }

    public CartSummaryResponse getSummary(Session session) {

        Cart cart = cartService.getOrCreateBySession(session);

        return cartProductRepository.getSummaryByCart(cart);
    }

    public CartSummaryResponse getSummary(Customer customer) {

        Cart cart = cartService.getOrCreateByCustomer(customer);

        return cartProductRepository.getSummaryByCart(cart);
    }
}
