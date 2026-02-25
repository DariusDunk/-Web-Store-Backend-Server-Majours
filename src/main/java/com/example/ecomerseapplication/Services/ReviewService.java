package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.DTOs.requests.ReviewCreateRequest;
import com.example.ecomerseapplication.DTOs.requests.ReviewUpdateRequest;
import com.example.ecomerseapplication.DTOs.requests.ReviewSortRequest;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.DTOs.responses.RatingOverviewResponse;
import com.example.ecomerseapplication.DTOs.responses.ReviewResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Review;
import com.example.ecomerseapplication.Repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReviewService {


    private final ReviewRepository reviewRepository;
    private final ProfanityService profanityService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ProfanityService profanityService) {
        this.reviewRepository = reviewRepository;
        this.profanityService = profanityService;
    }

    public void save(Review review) {
        reviewRepository.save(review);
    }

    @Transactional
    public void update(Review review) {
        reviewRepository.updateReview(review.getId(), review.getRating(), review.getReviewText());
    }

    public Boolean exists(Product product, Customer customer) {
//        return reviewRepository.existsByProductAndCustomer_KeycloakId(product, customer.getKeycloakId());
        return reviewRepository.existsByProductAndKID(product, customer.getKeycloakId());
    }

    public Review getByProdAndCust(Product product, Customer customer) {
        return reviewRepository.getByProductAndCustomer(product, customer).orElse(null);
    }

    public Review getByUIDAndPCode(String productCode, String customerId) {
        return reviewRepository.getReviewByCustomer_KeycloakIdAndProduct_ProductCode(customerId, productCode).orElse(null);//TODO vij sled migraciqta
    }

    @Nullable
    public ResponseEntity<?> requestValidation(Short rating, String reviewText) {
        if (rating > 5 || rating < 1) {
            System.out.println("INCORRECT RATING");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Само стойности от 1-5 са позволени!");
        }

        if (reviewText.length() > 500) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                    "Надвишен лимит",
                    HttpStatus.BAD_REQUEST.value(),
                    "Размера на коментара надвишава максималният размер"));
        }

//        if (profanityService.containsProfanity(reviewText))
//        {
////            System.out.println("Нецензорни думи");
//            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.VALIDATION_ERROR,
//                    "Засечен нецензурен език",
//                    HttpStatus.BAD_REQUEST.value(),
//                    "Засечен нецензурен език, ревюто отказано!"));
//        }
        return null;
    }

    @Transactional
    public Product createReview(Product product, Customer customer, ReviewCreateRequest request, Boolean isVerifiedCustomer) {

        short adjustedRating = (short) (request.rating() * 10);
        Review review = new Review();

        review.setProduct(product);
        review.setCustomer(customer);
        review.setReviewText(profanityService.censorProfanity(request.reviewText()));
        review.setRating(adjustedRating);
        review.setVerifiedCustomer(isVerifiedCustomer);
        review.setIsDeleted(false);

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
    public Product updateReview(Review existingReview, ReviewUpdateRequest request, Product product) {
        short adjustedRating = (short) (request.rating * 10);

//        System.out.println("EXISTING RATING: " + existingReview.getRating() + " NEW RATING: " + adjustedRating);

        if (existingReview.getRating() == adjustedRating && request.reviewText.equals(existingReview.getReviewText()))
            return null;

        existingReview.setRating(adjustedRating);
        existingReview.setReviewText(profanityService.censorProfanity(request.reviewText));
        update(existingReview);

        updateProductRating(existingReview, product, adjustedRating);
        return product;
    }

    private static void updateProductRating(Review existingReview, Product product, short adjustedRating) {
        if (product.getReviews().size() == 1)
            product.setRating(adjustedRating);

        else {
            short oldRating = 0;
            for (Review review : product.getReviews())
                oldRating += review.getRating();

            short newRating = (short) (((oldRating - existingReview.getRating()) + adjustedRating) / product.getReviews().size());

//            System.out.printf("NEW RATING CALCULATION: ((%d - %d) + %d)/%d = %d", oldRating, existingReview.getRating(), adjustedRating, product.getReviews().size(), newRating);

            product.setRating(newRating);
        }
    }

//    @Transactional
//    public void delete(Review review) {
//        reviewRepository.delete(review);//TODO tova trqbva da e dostypno samo za admina i trqbva da ima logika za update na reitinga
//    }

    public Page<ReviewResponse> getProductReviews(ReviewSortRequest request, Pageable pageable, String customerId) {

        Instant now = Instant.now();
        Instant twentyFourHoursAgo = now.minus(24, ChronoUnit.HOURS);

        if (request.verifiedOnly() == true) {
            return reviewRepository.getByProductCodeVerifiedOnly(
                    request.productCode(),
                    request.ratingValue(),
                    customerId,
                    pageable,
                    twentyFourHoursAgo
            );
        } else
            return reviewRepository.getByProductCodeAll(
                    request.productCode(),
                    request.ratingValue(),
                    customerId,
                    pageable,
                    twentyFourHoursAgo);

    }

    public List<RatingOverviewResponse> getRatingOverview(String productCode) {
        return reviewRepository.getRatingOverviewByProductCode(productCode);
    }

    public void softDelete(Review review) {
        review.setReviewText("");
        review.setIsDeleted(true);
        save(review);
    }
}



