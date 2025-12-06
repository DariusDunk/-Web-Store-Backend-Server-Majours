package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.ReviewRequest;
import com.example.ecomerseapplication.DTOs.requests.ReviewSortRequest;
import com.example.ecomerseapplication.DTOs.responses.RatingOverviewResponse;
import com.example.ecomerseapplication.DTOs.responses.ReviewResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Review;
import com.example.ecomerseapplication.Repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {


    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, CustomerService customerService, CustomerCartService customerCartService, PurchaseCartService purchaseCartService) {
        this.reviewRepository = reviewRepository;
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
    public Product manageReview(Product product, Customer customer, ReviewRequest request, Boolean isVerifiedCustomer) {

//        Review existingReview = getByProdCust(product, customer);

//        if (existingReview != null) {
//            return updateReview(existingReview, request, product);TODO kato se razdelqt syzdavaneto i updeita na revuta, tozi method mai 6te stane izli6en
//        }
        return createReview(product, customer, request, isVerifiedCustomer);
    }

    @Transactional
    public Product createReview(Product product, Customer customer, ReviewRequest request, Boolean isVerifiedCustomer) {

        short adjustedRating = (short) (request.rating * 10);
        Review review = new Review();

        review.setProduct(product);
        review.setCustomer(customer);
        review.setReviewText(request.reviewText);
        review.setRating(adjustedRating);
        review.setPostTimestamp(LocalDateTime.now());
        review.setVerifiedCustomer(isVerifiedCustomer);

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

        updateProductRating(existingReview, request, product, adjustedRating);
        return product;
    }

    private static void updateProductRating(Review existingReview, ReviewRequest request, Product product, short adjustedRating) {
        if (product.getReviews().size() == 1)
            product.setRating(request.rating);

        else {
            short oldRating = 0;
            for (Review review : product.getReviews())
                oldRating += review.getRating();

            short newRating = (short) (((oldRating - existingReview.getRating()) + adjustedRating) / product.getReviews().size());

            product.setRating(newRating);
        }
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

    public Page<ReviewResponse> getProductReviews(ReviewSortRequest request, Pageable pageable) {

        return reviewRepository.getByProductCode(request.productCode(),
                request.sortOrder().getValue(),
                request.verifiedOnly(),
                request.ratingValue(),
                request.userId(),
                pageable
                );
    }

    public List<RatingOverviewResponse> getRatingOverview(String productCode) {
        return reviewRepository.getRatingOverviewByProductCode(productCode);
    }
}



