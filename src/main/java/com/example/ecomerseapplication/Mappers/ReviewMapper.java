package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.ReviewContentResponse;
import com.example.ecomerseapplication.Entities.Review;

public class ReviewMapper {

    public static ReviewContentResponse entToContentResponse(Review review) {

        if (review != null) {
            return new ReviewContentResponse(review.getReviewText(), review.getRating(), true);
        }

        return new ReviewContentResponse(null, null, false);

    }
}
