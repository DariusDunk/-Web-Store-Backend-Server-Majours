package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.ReviewCreateRequest;
import com.example.ecomerseapplication.DTOs.requests.ReviewUpdateRequest;
import com.example.ecomerseapplication.DTOs.requests.ReviewSortRequest;
import com.example.ecomerseapplication.DTOs.responses.RatingOverviewResponse;
import com.example.ecomerseapplication.DTOs.responses.ReviewResponse;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Review;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.IncorrectRatingException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.ReviewTextLimitReachedException;
import com.example.ecomerseapplication.Repositories.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        return reviewRepository.getByProductAndCustomer(product, customer).orElseThrow(() -> new EntityNotFoundException("Review not found"));
    }

    public Review getByUIDAndPCode(String productCode, String customerId) {
        return reviewRepository.getReviewByCustomer_KeycloakIdAndProduct_ProductCode(customerId, productCode).orElse(null);
    }

    public void requestValidation(Short rating, String reviewText) {
        if (rating > 5 || rating < 1) {
            throw new IncorrectRatingException("Incorrect rating for review: '" + rating + "'");
        }

        if (reviewText.length() > 500) {
            throw new ReviewTextLimitReachedException("Review text exceeds the limit of 500 characters");
        }
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

        product.setReviewCount(product.getReviewCount() + 1);

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

    public Page<ReviewResponse> getProductReviewsForAuth(ReviewSortRequest request, Pageable pageable, String customerId) {

        Instant now = Instant.now();
        Instant twentyFourHoursAgo = now.minus(24, ChronoUnit.HOURS);

        if (request.verifiedOnly() == true) {
            return reviewRepository.getByProductCodeVerifiedOnlyForAuth(
                    request.productCode(),
                    request.ratingValue(),
                    customerId,
                    pageable,
                    twentyFourHoursAgo
            );
        } else
            return reviewRepository.getByProductCodeAllForAuth(
                    request.productCode(),
                    request.ratingValue(),
                    customerId,
                    pageable,
                    twentyFourHoursAgo);

    }

    public List<RatingOverviewResponse> getRatingOverview(String productCode) {
        return reviewRepository.getRatingOverviewByProductCode(productCode);
    }
    @Transactional
    public void softDelete(Review review) {
        review.setReviewText("");
        review.setIsDeleted(true);
        save(review);
    }

    public Page<ReviewResponse> getProductReviews(ReviewSortRequest request, PageRequest pageable, Session session)
    {
        try
        {
            if (!session.getIsGuest()) {
                String customerId = session.getCustomer().getKeycloakId();
                return getProductReviewsForAuth(request, pageable, customerId);
            } else {
                return getProductReviewsForGuest(request, pageable);
            }
        }
        catch (Exception e)
            {
            System.out.println("-------------------------Exception in product reviews endpoint-------------------------\n" + e.getMessage());
            throw e;
            }
    }


    public Page<ReviewResponse> getProductReviewsForGuest(@Valid ReviewSortRequest request, PageRequest pageable) {

        if (request.verifiedOnly() == true) {
            return reviewRepository.getByProductCodeVerifiedOnlyForGuest(
                    request.productCode(),
                    request.ratingValue(),
                    pageable);
        } else
            return reviewRepository.getByProductCodeAllForGuest(
                    request.productCode(),
                    request.ratingValue(),
                    pageable);
    }
}



