package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.CompositeIdClasses.FavoriteOfCustomerId;
import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.FavoriteOfCustomer;
import com.example.ecomerseapplication.Entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteOfCustomerRepository extends JpaRepository<FavoriteOfCustomer, FavoriteOfCustomerId> {

    Integer countAllByFavoriteOfCustomerId_Customer_keycloakId(String keycloakId);

    Boolean existsByFavoriteOfCustomerId_CustomerAndFavoriteOfCustomerId_Product(Customer customer, Product product);

    void deleteFavoriteOfCustomerByFavoriteOfCustomerId_CustomerAndFavoriteOfCustomerId_Product(Customer favoriteOfCustomerIdCustomer, Product favoriteOfCustomerIdProduct);

    @Query("""
            delete from FavoriteOfCustomer foc
            where foc.favoriteOfCustomerId.customer=:customer and foc.favoriteOfCustomerId.product.productCode in :productCodes
            """)
    @Modifying
    void deleteBatch(@Param("customer")Customer customer, @Param("productCodes")List<String> productCodes);

    @Query("""
            select new com.example.ecomerseapplication.DTOs.responses.CompactProductResponse(
                        foc.favoriteOfCustomerId.product.productCode,
                        foc.favoriteOfCustomerId.product.productName,
                        foc.favoriteOfCustomerId.product.originalPriceStotinki,
                        foc.favoriteOfCustomerId.product.salePriceStotinki,
                        foc.favoriteOfCustomerId.product.rating,
                        SIZE(foc.favoriteOfCustomerId.product.reviews),
                        foc.favoriteOfCustomerId.product.mainImageUrl,
                        case when foc.favoriteOfCustomerId.product.quantityInStock>0 then true else false end)
                        from FavoriteOfCustomer foc
                        where foc.favoriteOfCustomerId.customer.keycloakId = :customerId
                        order by foc.dateAdded desc
            """)//TODO zameni tazi zaqvka sys starata zaqvka sled kato priklu4is migriraneto
    Page<CompactProductResponse> getFromFavouritesPage(@Param("customerId") String  customer, Pageable pageable);//todo premesti v repositorito na lubimite, opravi i servica sled tova, kakto i kontrolera
}
