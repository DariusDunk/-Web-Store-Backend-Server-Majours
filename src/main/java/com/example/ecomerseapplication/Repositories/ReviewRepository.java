package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.responses.RatingOverviewResponse;
import com.example.ecomerseapplication.DTOs.responses.ReviewResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> getByProductAndCustomer(Product product, Customer customer);

    Optional<Review> getReviewByCustomer_KeycloakIdAndProduct_ProductCode(String customerKeycloakId, String productProductCode);

    @Modifying
    @Query(value = "update Review " +
            "set rating = :rating, reviewText = :text " +
            "where id = :id")
    void updateReview(@Param("id") long id, @Param("rating") short rating, @Param("text") String text);

    @Query(value = "select new com.example.ecomerseapplication.DTOs.responses.ReviewResponse(" +
            "r.id," +
            "r.reviewText, " +
            "r.rating, " +
            "case when r.isDeleted=true then null " +
            "else r.postTimestamp " +
            "end, " +
            "new com.example.ecomerseapplication.DTOs.responses.CustomerDetailsForReview(" +
            "case when " +
            "r.isDeleted=true then 'Ревюто е изтрито' " +
            "else r.customer.firstName||' '||r.customer.lastName " +
            "end, " +
            "case when " +
            "r.isDeleted=true then null else " +
            "r.customer.customerPfp " +
            "end," +
            "case " +
            "when r.customer.keycloakId = :customerId then true " +
            "else false " +
            "end, " +
            "case when r.isDeleted=true " +
            "then false " +
            "else r.verifiedCustomer " +
            "end, " +
            "case " +
            "when r.postTimestamp>=:aDayEarlier then false " +
            "else true " +
            "end)," +
            "r.isDeleted)" +
            "from Review r " +
            "where r.product.productCode = :productCode " +
            "and (:ratingValue IS NULL OR r.rating=:ratingValue) " +
            "and (:verifiedOnly=false or r.isDeleted=false and r.verifiedCustomer=true) "
    )
    Page<ReviewResponse> getByProductCode(
            @Param("productCode")
            String productCode,
            @Param("verifiedOnly")
            Boolean verifiedOnly,
            @Param("ratingValue")
            Short ratingValue,
            @Param("customerId")
            String customerId,
            Pageable pageable,
            @Param("aDayEarlier")
            Instant dayEarlier);

    @Query("""
            select distinct new com.example.ecomerseapplication.DTOs.responses.RatingOverviewResponse(r.rating, count(r))
            from Review r
            where r.product.productCode = ?1
            group by r.rating
            """)
    List<RatingOverviewResponse> getRatingOverviewByProductCode(String productCode);

    @Query("""
            select exists (select 1 from Review r
                        where r.product=:product and r.customer.keycloakId =:keycloakId)
            """)
    Boolean existsByProductAndKID(@Param("product")Product product, @Param("keycloakId")String keycloakId);
}
