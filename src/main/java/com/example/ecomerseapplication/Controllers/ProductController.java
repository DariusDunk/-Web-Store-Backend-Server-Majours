package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Auth.helpers.UserIdExtractor;
import com.example.ecomerseapplication.DTOs.requests.*;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.PostOrUpdateReviewForbiddenException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.ReviewSoftDeletedException;
import com.example.ecomerseapplication.Mappers.ReviewMapper;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorMessage;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Services.*;
import com.example.ecomerseapplication.Utils.SortHelper;
import com.example.ecomerseapplication.enums.ProductSortType;
import com.example.ecomerseapplication.enums.ReviewSortType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Validated
@RestController
@RequestMapping("product/")
public class ProductController {

    private final ProductService productService;
    private final CategoryAttributeService categoryAttributeService;
    private final CustomerService customerService;
    private final ReviewService reviewService;
    private final ProductCategoryService productCategoryService;
    private final ManufacturerService manufacturerService;
    private final PurchaseCartService purchaseCartService;
    private final UserIdExtractor userIdExtractor;

    @Autowired
    public ProductController(ProductService productService, CategoryAttributeService categoryAttributeService, CustomerService customerService, ReviewService reviewService, ProductCategoryService productCategoryService, ManufacturerService manufacturerService, PurchaseCartService purchaseCartService, UserIdExtractor userIdExtractor) {
        this.productService = productService;
        this.categoryAttributeService = categoryAttributeService;
        this.customerService = customerService;
        this.reviewService = reviewService;
        this.productCategoryService = productCategoryService;
        this.manufacturerService = manufacturerService;
        this.purchaseCartService = purchaseCartService;
        this.userIdExtractor = userIdExtractor;
    }

    @GetMapping("findall")
    public ResponseEntity<PageResponse<CompactProductResponse>> findAll(@RequestParam @NotNull int page) {

        Sort sort = SortHelper.buildProdSort(ProductSortType.POPULARITY.getValue());
        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit, sort);

        return ResponseEntity.ok(PageResponse.
                from(productService.findAllByRatingResponsePage(pageRequest))
        );
    }

    @GetMapping("search")
    public ResponseEntity<PageResponse<CompactProductResponse>> findByNameLike(@RequestParam @NotBlank String name,
                                                                               @NotNull @RequestParam int page,
                                                                               @RequestParam(required = false, name = "sort") String sortOrder
    ) {

        if (sortOrder == null || sortOrder.isBlank() || !ProductSortType.isValid(sortOrder)) {
            sortOrder = ProductSortType.RELEVANCE.getValue();
        }

        PageRequest pageRequest;
        Page<CompactProductResponse> responsePages;

        System.out.println("name: " + name + " sort: " + sortOrder + " page: " + page);

        if (sortOrder == null || sortOrder.equals(ProductSortType.RELEVANCE.getValue())) {
            pageRequest = PageRequest.of(page, PageContentLimit.limit);
//            productService.testRelevance(name);
            responsePages = productService.getProductsByRelevance(pageRequest, name);
        } else {
            pageRequest = PageRequest.of(page, PageContentLimit.limit);
            responsePages = productService.getProductsLikeNameSort(pageRequest, name, sortOrder);
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(PageResponse.from(responsePages));
    }

    @GetMapping("suggest")
    public ResponseEntity<List<String>> getNameSuggestions(@RequestParam @NotBlank String name) {
        return ResponseEntity.ok(productService.getNameSuggestions(name));
    }


    @GetMapping("{productCode}")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<DetailedProductResponse> detailedProductInfo(@PathVariable String productCode) {

        String id = userIdExtractor.getUserId();

        Customer customer = customerService.getById(id);

        return ResponseEntity.ok(productService.getByNameAndCode(productCode, customer));
    }

    @GetMapping("{productCode}/review/overview")
    public ResponseEntity<?> getReviewsOverview(@PathVariable String productCode) {
        return ResponseEntity.ok(reviewService.getRatingOverview(productCode));
    }

    @GetMapping("manufacturer/{manufacturerName}/p{page}")
    public ResponseEntity<PageResponse<CompactProductResponse>> productsByManufacturer(@PathVariable String manufacturerName,
                                                                                       @PathVariable int page,
                                                                                       @RequestParam(required = false, name = "sort") String sortOrder) {

//        System.out.println("Chosen sort: " + ((sortOrder!=null&&!sortOrder.isBlank())? sortOrder: "none") );

        Sort sort = (sortOrder != null && !sortOrder.isBlank())
                ? SortHelper.buildProdSort(ProductSortType.valueOf(sortOrder.toUpperCase()).getValue())
                : SortHelper.buildProdSort(ProductSortType.POPULARITY.getValue());

        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit, sort);
        Manufacturer manufacturer = manufacturerService.findByName(manufacturerName);

        Page<CompactProductResponse> productResponsePage = productService.getByManufacturer(manufacturer, pageRequest);

//        System.out.println("Sorted content: "+ productResponsePage.getContent());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(PageResponse.from(productResponsePage));
    }

    @GetMapping("category/{name}/p{page}")
    public ResponseEntity<PageResponse<CompactProductResponse>> getProductsByCategory(@PathVariable String name,
                                                                                      @PathVariable int page,
                                                                                      @RequestParam(required = false, name = "sort") String sortOrder) {

        if (name.equals("Бензинови машини") || name.equals("електрически машини"))//TODO TOVA 6TE TRQBVA DA GO MAHA6 KATO SLOJI6 NOVITE RODITELSKI KATEGORII
            return ResponseEntity.notFound().build();

//        System.out.println("Chosen sort: " + ((sortOrder!=null&&!sortOrder.isBlank())? sortOrder: "none") );

        Sort sort = (sortOrder != null && !sortOrder.isBlank())
                ? SortHelper.buildProdSort(ProductSortType.valueOf(sortOrder.toUpperCase()).getValue())
                : SortHelper.buildProdSort(ProductSortType.POPULARITY.getValue());

        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit, sort);

        ProductCategory productCategory = productCategoryService.findByName(name);

        Page<CompactProductResponse> productResponsePage = productService.getByCategory(productCategory, pageRequest);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(PageResponse.from(productResponsePage));
    }

    @PostMapping("filter/{page}")
    public ResponseEntity<PageResponse<CompactProductResponse>> productByFilterAndManufacturer(@RequestBody @Valid ProductFilterRequest productFilterRequest,
                                                                                               @PathVariable int page) {

        Sort sort = (productFilterRequest.sortOrder != null && !productFilterRequest.sortOrder.isBlank())
                ? SortHelper.buildProdSort(ProductSortType.valueOf(productFilterRequest.sortOrder.toUpperCase()).getValue())
                : SortHelper.buildProdSort(ProductSortType.POPULARITY.getValue());

        Set<CategoryAttribute> categoryAttributeSet = new HashSet<>();

        if (productFilterRequest.filterAttributes != null) {
            categoryAttributeSet = categoryAttributeService.getByNamesAndOptions(productFilterRequest.filterAttributes);
        }

        List<Manufacturer> manufacturerList = new ArrayList<>();

        if (productFilterRequest.manufacturerNames != null)
            manufacturerList = manufacturerService.getByNames(productFilterRequest.manufacturerNames);

        ProductCategory productCategory = productCategoryService.findByName(productFilterRequest.productCategory);

        PageRequest pageRequest = PageRequest.of(page, 10, sort);

        return ResponseEntity.ok(PageResponse.from(
                productService.getByCategoryFiltersManufacturerAndPriceRange(
                        categoryAttributeSet,
                        productCategory,
                        productFilterRequest.priceLowest,
                        productFilterRequest.priceHighest,
                        manufacturerList,
                        productFilterRequest.rating,
                        pageRequest)));
    }

    @PostMapping("reviews/paged")
    @PreAuthorize("hasRole(@roles.customer())")//todo tuk trqbva tokena da e optional i da ne se polzva PreAuthorize
    public ResponseEntity<PageResponse<ReviewResponse>> getPagedReviews(@RequestBody @Valid ReviewSortRequest request) {

        String customerId = userIdExtractor.getUserId();

        Sort sort = request.sortOrder().getValue().equalsIgnoreCase(ReviewSortType.NEWEST.getValue())
                ? Sort.by("postTimestamp").descending()
                : Sort.by("postTimestamp").ascending();

        PageRequest pageRequest = PageRequest.of(request.page(), PageContentLimit.limit, sort);

//        System.out.println(request);

        Page<ReviewResponse> reviewPage = reviewService.getProductReviews(request, pageRequest, customerId);

//        System.out.println(reviewPage.getContent());

        return ResponseEntity.ok(PageResponse.from(reviewPage));
    }

    @GetMapping("review/specific")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> getSpecificReviewData(@RequestParam("productCode") @NotBlank String productCode) {//todo produlji custom exception rabotata ot tuk

//        System.out.println("IUD " + userId + " PCODE " + productCode);
        String customerId = userIdExtractor.getUserId();

        Review review = reviewService.getByUIDAndPCode(productCode, customerId);

//        System.out.println("REVIEW: " + review.getId() );
        if (review != null) {
            if (review.getIsDeleted()) {
                throw new ReviewSoftDeletedException("Review is soft deleted");
            } else {
                if (isUpdateTimeOver(review))
                    throw new PostOrUpdateReviewForbiddenException("This user cannot post a new review for this product, or update the existing one");

                ReviewContentResponse response = ReviewMapper.entToContentResponse(review);
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.ok(new ReviewContentResponse("", null, false));
    }

    @PostMapping("review/add")
    @Transactional
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> addReview(@RequestBody @Valid ReviewCreateRequest request) {

        reviewService.requestValidation(request.rating(), request.reviewText());

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(userId);
        Product product = productService.findByPCode(request.productCode());

        if (reviewService.exists(product, customer)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.RESOURCE_ALREADY_EXISTS,
                    "Заявката е отказана",
                    HttpStatus.CONFLICT.value(),
                    "Вече имате ревю за този продукт"));
        }

        Boolean isVerifiedCustomer = purchaseCartService.isProductPurchased(product.getProductCode(), customer.getKeycloakId());

        Product updatedProduct = reviewService.createReview(product, customer, request, isVerifiedCustomer);

        productService.save(updatedProduct);

        return ResponseEntity.status(HttpStatus.CREATED).body("Ревюто е качено!");
    }

    @PatchMapping("review/update")
    @Transactional
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> updateReview(@RequestBody @Valid ReviewUpdateRequest request) {

        reviewService.requestValidation(request.rating, request.reviewText);

        String customerId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(customerId);
        Product product = productService.findByPCode(request.productCode);
        Review review = reviewService.getByProdAndCust(product, customer);

        if (review.getIsDeleted()) {
            return ResponseEntity.notFound().build();
        }

        if (isUpdateTimeOver(review))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ErrorType.RESOURCE_ALREADY_EXISTS,
                    "Не може да се добавят повече ревюта",
                    HttpStatus.FORBIDDEN.value(),
                    "Вече сте добавили ревю за този продукт. Срокът за редакция е изтекъл и не могат да се правят промени."));

        Product updatedProduct = reviewService.updateReview(review, request, product);//todo po4i cqlata logika po update-a trqbva da e tuk

        if (updatedProduct == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ErrorType.DUPLICATION_OF_DATA,
                    "Не бе извършена промяна",
                    HttpStatus.BAD_REQUEST.value(),
                    ErrorMessage.DUPLICATION_OF_REVIEW_DATA));
        }

        try {
            productService.save(updatedProduct);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();

    }

    private boolean isUpdateTimeOver(Review review) {
        Instant now = Instant.now();
        Instant deadline = review.getPostTimestamp().plus(24, ChronoUnit.HOURS);

        return now.isAfter(deadline);
    }

    @DeleteMapping("review/delete")
    @Transactional
    public ResponseEntity<String> deleteReview(@RequestParam("product_code") @NotBlank String productCode) {

        String customerId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(customerId);
        Product product = productService.findByPCode(productCode);
        Review review = reviewService.getByProdAndCust(product, customer);

        if (review.getIsDeleted())
            return ResponseEntity.notFound().build();

//        short newRating = reviewService.updatedRating(product, review);
//
//        if (newRating == -1)
//            return ResponseEntity.internalServerError().build();

//        product.setRating(newRating);
//        product.getReviews().remove(review);
//        productService.save(product); todo tova moje da se sloji za drug method, moje bi adminski, koito specialno iztriva review-ta ZADULJITELNO SLOJI UPDATE NA POLETO ZA BROI NA REVIEW-TATA KATO SE IZTRIE OKON4ATELNO REVIEW ZA PRODUKTA!!!
        try {
            reviewService.softDelete(review);
        } catch (Exception e) {
            System.out.println("Error soft deleting review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().body("Ревюто е изтрито");
    }
}
