package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.ReviewCustomerDTO;
import com.example.ecomerseapplication.DTOs.ReviewDTO;
import com.example.ecomerseapplication.DTOs.requests.ReviewRequest;
import com.example.ecomerseapplication.DTOs.responses.CustomerDetailsForReview;
import com.example.ecomerseapplication.DTOs.responses.ReviewResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Review;
import com.example.ecomerseapplication.Mappers.ReviewMapper;
import com.example.ecomerseapplication.Repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {


    private final ReviewRepository reviewRepository;
    private final CustomerService customerService;
    private final CustomerCartService customerCartService;
    private final PurchaseCartService purchaseCartService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, CustomerService customerService, CustomerCartService customerCartService, PurchaseCartService purchaseCartService) {
        this.reviewRepository = reviewRepository;
        this.customerService = customerService;
        this.customerCartService = customerCartService;
        this.purchaseCartService = purchaseCartService;
    }

    public void save(Review review) {
        reviewRepository.save(review);
    }

    @Transactional
    public void update(Review review) {
        reviewRepository.updateReview(review.getId(), review.getRating(), review.getReviewText());
    }

    public boolean exists(Product product, Customer customer) {
        return reviewRepository.existsByProductAndCustomer(product, customer);
    }

    public Review getByProdCust(Product product, Customer customer) {
        return reviewRepository.getByProductAndCustomer(product, customer).orElse(null);
    }

    @Transactional
    public Product manageReview(Product product, Customer customer, ReviewRequest request) {

        Review existingReview = getByProdCust(product, customer);

        if (existingReview != null) {
            return updateReview(existingReview, request, product);
        }
        return createReview(product, customer, request);
    }

    @Transactional
    public Product createReview(Product product, Customer customer, ReviewRequest request) {

        short adjustedRating = (short) (request.rating * 10);
        Review review = new Review();

        review.setProduct(product);
        review.setCustomer(customer);
        review.setReviewText(request.reviewText);
        review.setRating(adjustedRating);

        if (product.getReviews().isEmpty())
            product.setRating(adjustedRating);

        else {
            short rating = (short) (((product.getRating() * product.getReviews().size()) + adjustedRating) / (product.getReviews().size() + 1));
            product.setRating(rating);
        }

        save(review);

        return product;
    }

    @Transactional
    public Product updateReview(Review existingReview, ReviewRequest request, Product product) {
        short adjustedRating = (short) (request.rating * 10);

        if (existingReview.getRating() == adjustedRating
                && existingReview.getReviewText().equals(request.reviewText))
            return null;

        existingReview.setRating(adjustedRating);
        existingReview.setReviewText(request.reviewText);
        update(existingReview);

        if (product.getReviews().size() == 1)
            product.setRating(request.rating);

        else {
            short oldRating = 0;
            for (Review review : product.getReviews())
                oldRating += review.getRating();

            short newRating = (short) (((oldRating - existingReview.getRating()) + adjustedRating) / product.getReviews().size());

            product.setRating(newRating);
        }
        return product;
    }

    public Short updatedRating(Product product, Review review) {

        if (review == null)
            return -1;

        short oldRating = 0;

        for (Review rev : product.getReviews())
            oldRating += rev.getRating();

        return (short) ((oldRating - review.getRating()) / (product.getReviews().size() - 1));
    }

    @Transactional
    public void delete(Review review) {
        reviewRepository.delete(review);
    }

    public List<ReviewResponse> getProductReviews(String productCode, long userId) {
        List<ReviewDTO> reviews = reviewRepository.getByProductCode(productCode);

        List<Long> userIds = new ArrayList<>();

        for (ReviewDTO review : reviews) {
            userIds.add(review.customer().customerId());
        }

        List<Long> verifiedCustomers = purchaseCartService.isProductPurchased(productCode, userIds);
        userIds.clear();

        return ReviewMapper.revDtoListToResponseList(reviews, verifiedCustomers, userId);
    }
}



