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

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Boolean existsByProductAndCustomer(Product product, Customer customer);

    Optional<Review> getByProductAndCustomer(Product product, Customer customer);
    Optional<Review> getReviewByCustomer_IdAndProduct_ProductCode(Long customerId, String productCode);

    @Modifying
    @Query(value = "update Review " +
            "set rating = :rating, reviewText = :text " +
            "where id = :id")
    void updateReview(@Param("id") long id, @Param("rating") short rating, @Param("text") String text);

    @Query(value = "select new com.example.ecomerseapplication.DTOs.responses.ReviewResponse(" +
            "r.id," +
            "r.reviewText, " +
            "r.rating, " +
            "r.postTimestamp, " +
            "new com.example.ecomerseapplication.DTOs.responses.CustomerDetailsForReview(" +
            "r.customer.name," +
            "r.customer.customerPfp," +
            "case " +
            "when r.customer.id = :customerId then true " +
            "else false " +
            "end, " +
            "r.verifiedCustomer))" +
            "from Review r " +
            "where r.product.productCode = :productCode " +
            "and (:ratingValue IS NULL OR r.rating=:ratingValue) " +
            "and (:verifiedOnly=false or r.verifiedCustomer=true) " +
            "order by " +
            "case when :sortOrder = 'newest' then r.postTimestamp end asc, " +
            "case when :sortOrder = 'oldest' then r.postTimestamp end desc")
    Page<ReviewResponse> getByProductCode(
            @Param("productCode")
            String productCode,
            @Param("sortOrder")
            String sortOrder,
            @Param("verifiedOnly")
            Boolean verifiedOnly,
            @Param("ratingValue")
            Short ratingValue,
            @Param("customerId")
            Long customerId,
            Pageable pageable);

    @Query("""
            select distinct new com.example.ecomerseapplication.DTOs.responses.RatingOverviewResponse(r.rating, count(r))
            from Review r
            where r.product.productCode = ?1
            group by r.rating
            """)
    List<RatingOverviewResponse> getRatingOverviewByProductCode(String productCode);
}
