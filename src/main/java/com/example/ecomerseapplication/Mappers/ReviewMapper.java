package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.CustomerDetailsForReview;
import com.example.ecomerseapplication.DTOs.responses.ReviewResponse;
import com.example.ecomerseapplication.Entities.Review;

public class ReviewMapper {

    public static ReviewResponse entityToResponse(Review review) {
        ReviewResponse reviewResponse = new ReviewResponse();
        reviewResponse.reviewText = review.getReviewText();
        reviewResponse.rating = review.getRating();

        CustomerDetailsForReview customerDetailsForReview = new CustomerDetailsForReview();
        customerDetailsForReview.customerPfp = review.getCustomer().getCustomerPfp();
        customerDetailsForReview.name = review.getCustomer().getName();

        reviewResponse.customerDetailsForReview = customerDetailsForReview;

        return reviewResponse;
    }

    public static ReviewResponse entityToResponse2(Review review, long userIid) {
        ReviewResponse reviewResponse = new ReviewResponse();
        reviewResponse.reviewText = review.getReviewText();
        reviewResponse.rating = review.getRating();

        CustomerDetailsForReview customerDetailsForReview = new CustomerDetailsForReview();
        customerDetailsForReview.customerPfp = review.getCustomer().getCustomerPfp();
        customerDetailsForReview.name = review.getCustomer().getName();
        customerDetailsForReview.currentUser = review.getCustomer().getId() == userIid;

        reviewResponse.customerDetailsForReview = customerDetailsForReview;

        return reviewResponse;
    }

}
