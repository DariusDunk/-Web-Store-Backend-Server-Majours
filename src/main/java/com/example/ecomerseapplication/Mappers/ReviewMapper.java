package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.ReviewDTO;
import com.example.ecomerseapplication.DTOs.responses.CustomerDetailsForReview;
import com.example.ecomerseapplication.DTOs.responses.ReviewResponse;
import com.example.ecomerseapplication.Entities.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public static List<ReviewResponse> revDtoListToResponseList(List<ReviewDTO> reviews, List<Long> verifiedCustomers, long currentUserId) {

        Set<Long> verifiedCustSet = Set.copyOf(verifiedCustomers);

        return reviews.stream().map( review ->{
            long currentCustomerId = review.customer().customerId();
            boolean currUser =  (currentCustomerId == currentUserId);
            boolean verified =  verifiedCustSet.contains(currentCustomerId);

            return new ReviewResponse
                    (review.reviewText(),
                            review.rating(),
                            new CustomerDetailsForReview(review.customer().name(),
                                    review.customer().customerPfp(),
                                    currUser,
                                    verified)
                    );

        }).toList();
    }
}
