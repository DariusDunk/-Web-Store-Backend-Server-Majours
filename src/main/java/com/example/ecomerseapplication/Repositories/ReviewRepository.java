package com.example.ecomerseapplication.Repositories;

import com.example.ecomerseapplication.DTOs.ReviewDTO;
import com.example.ecomerseapplication.DTOs.responses.ReviewResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByProductAndCustomer(Product product, Customer customer);

    Optional<Review> getByProductAndCustomer(Product product, Customer customer);

    @Modifying
    @Query(value = "update Review " +
            "set rating = :rating, reviewText = :text " +
            "where id = :id")
    void updateReview(@Param("id") long id, @Param("rating") short rating, @Param("text") String text);


//    @Query(value = "select new com.example.ecomerseapplication.DTOs.responses.ReviewResponse(" +
//            "r.reviewText, " +
//            "r.rating, " +
//            "new com.example.ecomerseapplication.DTOs.responses.CustomerDetailsForReview(" +
//            "r.customer.name," +
//            "r.customer.customerPfp))" +
//            "from Review r" +
//            " where r.product.productCode = :productCode")
//    List<ReviewResponse> getByProductCode(String productCode);

    @Query(value = "select new com.example.ecomerseapplication.DTOs.ReviewDTO(" +
            "r.id," +
            "r.reviewText, " +
            "r.rating, " +
            "new com.example.ecomerseapplication.DTOs.ReviewCustomerDTO(" +
            "r.customer.name," +
            "r.customer.customerPfp," +
            "r.customer.id))" +
            "from Review r" +
            " where r.product.productCode = :productCode")
    List<ReviewDTO> getByProductCode(String productCode);
}
