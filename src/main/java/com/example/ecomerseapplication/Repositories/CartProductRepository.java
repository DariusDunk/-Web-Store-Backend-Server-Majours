package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.CartProductId;
import com.example.ecomerseapplication.DTOs.responses.CartSummaryResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.CartItemDTO;
import com.example.ecomerseapplication.Entities.Cart;
import com.example.ecomerseapplication.Entities.CartProduct;
import com.example.ecomerseapplication.Entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, CartProductId> {

    boolean existsByCartProductId(CartProductId cartProductId);

//    @Query(value = "select cc " +
//            "from CustomerCart cc " +
//            "where cc.cartProductId.customer = ?1")
//    List<CustomerCart> findByCustomer(Customer customer);

    @Query(value = "select cc " +
            "from CartProduct cc " +
            "where cc.cartProductId.cart.customer.keycloakId = ?1")
    List<CartProduct> findByCustomer(String customer);

    @Query(
            """
                    select new com
                    .example
                    .ecomerseapplication
                    .DTOs
                    .serverDtos
                    .CartItemDTO(
                      new com
                     .example
                     .ecomerseapplication
                     .DTOs
                     .serverDtos
                     .CompactProductDto(p.productCode,
                     p.productName,
                     p.originalPriceStotinki,
                     s.discountPercent,
                     sp.overrideDiscountPercentage,
                     p.rating,
                     size(p.reviews),
                     p.mainImageUrl,
                     case when p.quantityInStock>0 then true else false end
                     ),
                    cc.dateAdded,
                    cc.quantity)
                    from CartProduct cc
                    join Product p on p = cc.cartProductId.product
                    left join p.saleProducts sp
                    left join sp.sale s
                    with s.isActive = true
                    AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate
                    where cc.cartProductId.cart.customer.keycloakId =:keycloakId
                    order by
                    case
                        when cc.cartProductId.product.quantityInStock>0
                        then 1
                        else 0
                    end desc,
                    cc.dateAdded desc
                    """
    )
    List<CartItemDTO> findDtoByCustomer(@Param("keycloakId") String customer);

    @Modifying
    @Query("delete from CartProduct where cartProductId.cart = ?1 and cartProductId.product.productCode = ?2 ")
    void deleteByCartAndProductCode(Cart cart, String productCode);

    @Modifying
    @Query("""
            delete
            from
            CartProduct cc
            where cc.cartProductId.cart=:cart
            and cc.cartProductId.product.productCode in :productCodes
            """)
    void deleteBatchByCartAndPCodes(@Param("cart") Cart cart, @Param("productCodes") List<String> productCodes);

    @Query(value = "select cc " +
            "from CartProduct cc " +
            "where cc.cartProductId.cart.session = ?1")
    List<CartProduct> getBySession(Session session);


    @Query(
            """
                    select new com
                    .example
                    .ecomerseapplication
                    .DTOs
                    .serverDtos
                    .CartItemDTO(
                      new com
                     .example
                     .ecomerseapplication
                     .DTOs
                     .serverDtos
                     .CompactProductDto(p.productCode,
                     p.productName,
                     p.originalPriceStotinki,
                     s.discountPercent,
                     sp.overrideDiscountPercentage,
                     p.rating,
                     size(p.reviews),
                     p.mainImageUrl,
                     case when p.quantityInStock>0 then true else false end
                     ),
                    cc.dateAdded,
                    cc.quantity)
                    from CartProduct cc
                    join Product p on p = cc.cartProductId.product
                    left join p.saleProducts sp
                    left join sp.sale s
                    with s.isActive = true
                    AND CURRENT_TIMESTAMP BETWEEN s.startDate AND s.endDate
                    where cc.cartProductId.cart.session =:session
                    order by
                    case
                        when cc.cartProductId.product.quantityInStock>0
                        then 1
                        else 0
                    end desc,
                    cc.dateAdded desc
                    """
    )
    List<CartItemDTO> findDtoBySession(@Param("session")Session session);


    //todo tuk kato napravi6 otstupkite smeni izto4nika za cenite
    @Query(value = "select new " +
            "com.example.ecomerseapplication.DTOs.responses.CartSummaryResponse(sum(cc.cartProductId.product.salePriceStotinki*cc.quantity), sum(cc.quantity)) " +
            "from CartProduct cc " +
            "where cc.cartProductId.cart = ?1")
    CartSummaryResponse getSummaryByCart(Cart cart);
//
//    @Modifying
//    void deleteCartProductByCustomerCartId_Cart(Cart customerCartIdCart);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
                    delete
                    from CartProduct cp
                    where cp.cartProductId.cart.session.sessionId in ?1
                    """
    )
    void deleteCartProductsBySessions(List<String> sessionIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CartProduct where cartProductId.cart.session.sessionId = ?1")
    void deleteBySessionId(String sessionId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
update CartProduct cp
set cp.cartProductId.cart = :customerCart
where cp.cartProductId.cart = :sessionCart
and cp.cartProductId.product.id in :productIds
""")
    void transferNewGuestItemsToCustomer(
            @Param("customerCart") Cart customerCart,
            @Param("sessionCart") Cart sessionCart,
            @Param("productIds") List<Integer> productIds
    );
}
