package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.CustomerCartId;
import com.example.ecomerseapplication.DTOs.responses.CartItemResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, CustomerCartId> {

    boolean existsByCustomerCartId(CustomerCartId customerCartId);

//    @Query(value = "select cc " +
//            "from CustomerCart cc " +
//            "where cc.customerCartId.customer = ?1")
//    List<CustomerCart> findByCustomer(Customer customer);

    @Query(value = "select cc " +
            "from CartProduct cc " +
            "where cc.customerCartId.cart.customer.keycloakId = ?1")
    List<CartProduct> findByCustomer(String customer);

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
                    from CartProduct cc
                    join Product p on p = cc.customerCartId.product
                    where cc.customerCartId.cart.customer.keycloakId =:keycloakId
                    order by
                    case
                        when cc.customerCartId.product.quantityInStock>0
                        then 1
                        else 0
                    end desc,
                    cc.dateAdded desc
                    """
    )
    List<CartItemResponse> findDtoByCustomer(@Param("keycloakId") String customer);

//    @Modifying
//    @Query("delete from CustomerCart " +
//            "where customerCartId.customer = ?1")
//    void deleteAllByCustomer(Customer customer);


    @Modifying
    @Query("delete from CartProduct where customerCartId.cart.customer = ?1 and customerCartId.product.productCode = ?2 ")
    int deleteByCustomerAndProductCode(Customer customer, String productCode);

    @Modifying
    @Query("""
            delete
            from
            CartProduct cc
            where cc.customerCartId.cart.customer=:customer
            and cc.customerCartId.product.productCode in :productCodes
            """)
    int deleteBatchByCustomerAndPCodes(@Param("customer") Customer customer, @Param("productCodes") List<String> productCodes);
}
