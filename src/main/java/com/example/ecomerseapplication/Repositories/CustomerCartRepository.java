package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.CustomerCartId;
import com.example.ecomerseapplication.DTOs.responses.CartItemResponse;
import com.example.ecomerseapplication.DTOs.responses.CompactProductQuantityPairResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.CustomerCart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerCartRepository extends JpaRepository<CustomerCart, CustomerCartId> {

    @Modifying
    @Query(value = "update online_shop.customer_carts " +
            "set quantity = :quantity " +
            "where customer_id = :customerId and product_id = :productId", nativeQuery = true)
    void updateQuantity(@Param("quantity") short quantity,
                        @Param("customerId") long customerId,
                        @Param("productId") int productId);

    boolean existsByCustomerCartId(CustomerCartId customerCartId);

    @Query(value = "select cc " +
            "from CustomerCart cc " +
            "where cc.customerCartId.customer = ?1")
    List<CustomerCart> findByCustomer(Customer customer);

//    @Query(value = "select cc " +
//            "from CustomerCart cc " +
//            "where cc.customerCartId.customer = ?1")
//    Page<CustomerCart> findByCustomerPaged(Customer customer, Pageable pageable);

    @Query(
            """
            select new com
            .example
            .ecomerseapplication
            .DTOs
            .responses
            .CartItemResponse(
              new com
             .example
             .ecomerseapplication
             .DTOs
             .responses
             .CompactProductResponse(p.productCode,
             p.productName,
             p.originalPriceStotinki,
             p.salePriceStotinki,
             p.rating,
             size(p.reviews),
             p.mainImageUrl,
             case when p.quantityInStock>0 then true else false end
             ),
            cc.dateAdded,
            cc.quantity)
            from CustomerCart cc
            join Product p on p = cc.customerCartId.product
            order by cc.dateAdded desc
            """
    )
    List<CartItemResponse> findByCustomerPaged(Customer customer);

    @Modifying
    @Query("delete from CustomerCart " +
            "where customerCartId.customer = ?1")
    void deleteAllByCustomer(Customer customer);



}
