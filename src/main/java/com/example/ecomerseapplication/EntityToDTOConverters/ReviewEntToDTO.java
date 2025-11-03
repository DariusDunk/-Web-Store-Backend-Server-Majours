package com.example.ecomerseapplication.EntityToDTOConverters;

import com.example.ecomerseapplication.DTOs.requests.CustomerDetailsForReview;
import com.example.ecomerseapplication.DTOs.responses.ReviewResponse;
import com.example.ecomerseapplication.Entities.Review;

public class ReviewEntToDTO {

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
}
