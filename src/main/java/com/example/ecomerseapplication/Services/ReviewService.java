package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.DTOs.requests.ReviewRequest;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {


    private final ReviewRepository reviewRepository;
    private final ProfanityService profanityService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, CustomerService customerService, CustomerCartService customerCartService, PurchaseCartService purchaseCartService, ProfanityService profanityService) {
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

    public Review getByUIDAndPCode(String productCode, Long customerId) {
        return reviewRepository.getReviewByCustomer_IdAndProduct_ProductCode(customerId, productCode).orElse(null);
    }

    @Nullable
    public ResponseEntity<?> requestValidation(ReviewRequest request) {
        if (request.rating > 5 || request.rating < 1) {
            System.out.println("INCORRECT RATING");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Само стойности от 1-5 са позволени!");
        }

        if (request.reviewText.length() > 500) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                    "Надвишен лимит",
                    HttpStatus.BAD_REQUEST.value(),
                    "Размера на коментара надвишава максималният размер"));
        }

        if (profanityService.containsProfanity(request.reviewText))
        {
//            System.out.println("Нецензорни думи");
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.VALIDATION_ERROR,
                    "Засечен нецензурен език",
                    HttpStatus.BAD_REQUEST.value(),
                    "Засечен нецензурен език, ревюто отказано!"));
        }
        return null;
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
    public Product updateReview(Review existingReview, ReviewRequest request, Product product) {
        short adjustedRating = (short) (request.rating * 10);

//        System.out.println("EXISTING RATING: " + existingReview.getRating() + " NEW RATING: " + adjustedRating);

        if (existingReview.getRating() == adjustedRating && request.reviewText.equals(existingReview.getReviewText()))
            return null;

        existingReview.setRating(adjustedRating);
        existingReview.setReviewText(request.reviewText);
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
        reviewRepository.delete(review);//TODO sloji logika za obnovqvane na ratinga na produkta
    }

    public Page<ReviewResponse> getProductReviews(ReviewSortRequest request, Pageable pageable) {

        LocalDateTime startDate = LocalDate.now().atStartOfDay();
        LocalDateTime endDate = LocalDate.now().plusDays(1).atStartOfDay();

        return reviewRepository.getByProductCode(request.productCode(),
                request.sortOrder().getValue(),
                request.verifiedOnly(),
                request.ratingValue(),
                request.userId(),
                pageable,
                startDate,
                endDate
                );
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



