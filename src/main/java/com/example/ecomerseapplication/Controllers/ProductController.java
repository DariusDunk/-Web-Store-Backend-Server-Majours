package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.CustomerProductPairRequest;
import com.example.ecomerseapplication.DTOs.requests.ProductFilterRequest;
import com.example.ecomerseapplication.DTOs.requests.ReviewRequest;
import com.example.ecomerseapplication.DTOs.requests.ReviewSortRequest;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Others.ErrorMessage;
import com.example.ecomerseapplication.Others.ErrorType;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;


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

    @Autowired
    public ProductController(ProductService productService, CategoryAttributeService categoryAttributeService, CustomerService customerService, ReviewService reviewService, ProductCategoryService productCategoryService, ManufacturerService manufacturerService, PurchaseCartService purchaseCartService) {
        this.productService = productService;
        this.categoryAttributeService = categoryAttributeService;
        this.customerService = customerService;
        this.reviewService = reviewService;
        this.productCategoryService = productCategoryService;
        this.manufacturerService = manufacturerService;
        this.purchaseCartService = purchaseCartService;
    }

    @GetMapping("findall")
    public ResponseEntity<PageResponse<CompactProductResponse>> findAll(@RequestParam int page) {
        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        return ResponseEntity.ok(PageResponse.
                from(productService.findAllByRatingResponsePage(pageRequest))
        );
    }

    @GetMapping("search")
    public ResponseEntity<PageResponse<CompactProductResponse>> findByNameLike(@RequestParam String name, @RequestParam int page) {
        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        Page<CompactProductResponse> responsePages = productService.getProductsLikeName(pageRequest, name);

        if (responsePages.isEmpty())
            return ResponseEntity.notFound().build();

//        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responsePages);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(PageResponse.from(responsePages));
    }

    @GetMapping("suggest")
    public List<String> getNameSuggestions(@RequestParam String name) {
        return productService.getNameSuggestions(name);
    }

    @GetMapping("{productCode}")
    public ResponseEntity<DetailedProductResponse> detailedProductInfo(@PathVariable String productCode,
                                                                       @RequestParam long id) {


        Customer customer = customerService.findById(id);

        ResponseEntity<DetailedProductResponse> detailedProductResponse = productService
                .getByNameAndCode(productCode, customer);

        assert detailedProductResponse.getBody() != null;
//        System.out.println("PRODUCT: " + detailedProductResponse.getBody().categoryName);

        return Objects.requireNonNullElseGet(detailedProductResponse, () -> ResponseEntity.notFound().build());
    }

    @PostMapping("reviews/paged")
    public ResponseEntity<PageResponse<ReviewResponse>> getPagedReviews(@RequestBody ReviewSortRequest request) {

        PageRequest pageRequest = PageRequest.of(request.page(), PageContentLimit.limit);

//        System.out.println(request);

        Page<ReviewResponse> reviewPage = reviewService.getProductReviews(request, pageRequest);

//        System.out.println(reviewPage.getContent());

        return ResponseEntity.ok(PageResponse.from(reviewPage));

    }

    @GetMapping("{productCode}/review/overview")
    public ResponseEntity<?> getReviewsOverview(@PathVariable String productCode) {
        return ResponseEntity.ok(reviewService.getRatingOverview(productCode));
    }


    @GetMapping("manufacturer/{manufacturerName}/p{page}")
    public ResponseEntity<PageResponse<CompactProductResponse>> productsByManufacturer(@PathVariable String manufacturerName,
                                                                                       @PathVariable int page) {
        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);
        Manufacturer manufacturer = manufacturerService.findByName(manufacturerName);

        if (manufacturer == null) {
            return ResponseEntity.notFound().build();
        }

        Page<CompactProductResponse> productResponsePage = productService.getByManufacturer(manufacturer, pageRequest);

        if (productResponsePage.isEmpty())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(PageResponse.from(productResponsePage));
    }


    @GetMapping("category/{name}/p{page}")
    public ResponseEntity<PageResponse<CompactProductResponse>> getProductsByCategory(@PathVariable String name,
                                                                                      @PathVariable int page) {

        if (name.equals("Бензинови машини") || name.equals("електрически машини"))//TODO TOVA 6TE TRQBVA DA GO MAHA6 KATO SLOJI6 NOVITE RODITELSKI KATEGORII
            return ResponseEntity.notFound().build();

        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        ProductCategory productCategory = productCategoryService.findByName(name);
        if (productCategory == null) {
            return ResponseEntity.notFound().build();
        }

        Page<CompactProductResponse> productResponsePage = productService.getByCategory(productCategory, pageRequest);

        if (productResponsePage.isEmpty())
            return ResponseEntity.notFound().build();

//        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(productResponsePage);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(PageResponse.from(productResponsePage));

    }

    @PostMapping("filter/{page}")
    public ResponseEntity<PageResponse<CompactProductResponse>> productByFilterAndManufacturer(@RequestBody ProductFilterRequest productFilterRequest,
                                                                                               @PathVariable int page) {
        Set<CategoryAttribute> categoryAttributeSet = new HashSet<>();

        if (productFilterRequest.filterAttributes != null) {
            categoryAttributeSet = categoryAttributeService.getByNamesAndOptions(productFilterRequest.filterAttributes);
        }

        List<Manufacturer> manufacturerList = new ArrayList<>();

        if (productFilterRequest.manufacturerNames != null)
            manufacturerList = manufacturerService.getByNames(productFilterRequest.manufacturerNames);

        ProductCategory productCategory = productCategoryService.findByName(productFilterRequest.productCategory);

        if (productCategory == null) {
            return ResponseEntity.badRequest().build();
        }

        PageRequest pageRequest = PageRequest.of(page, 10);

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

    @PostMapping("review/add")
    @Transactional
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request) {

        System.out.println(request.toString());

        if (request.rating > 5 || request.rating < 1) {
            System.out.println("INCORRECT RATING");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Само стойности от 1-5 са позволени!");
        }

        Customer customer = customerService.findById(request.customerId);

        if (customer == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Product product = productService.findByPCode(request.productCode);

        if (product == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Review existingReview = reviewService.getByProdCust(product, customer);

        if (existingReview != null)
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.RESOURCE_ALREADY_EXISTS,
                    "Request canceled",
                    HttpStatus.CONFLICT.value(),
                    ErrorMessage.REVIEW_EXISTS));
        }

        Boolean isVerifiedCustomer = purchaseCartService.isProductPurchased(product.getProductCode(), customer.getId());

        Product updatedProduct = reviewService.createReview(product, customer, request, isVerifiedCustomer);

        if (updatedProduct == null)
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Не беше извършена промяна");

        productService.save(updatedProduct);

        return ResponseEntity.status(HttpStatus.CREATED).body("Ревюто е качено!");
    }

    @PatchMapping("review/update")
    @Transactional
    public ResponseEntity<?> updateReview(@RequestBody ReviewRequest request) {
        Customer customer = customerService.findById(request.customerId);

        if (customer == null)
            return ResponseEntity.notFound().build();

        Product product = productService.findByPCode(request.productCode);

        if (product == null)
            return ResponseEntity.notFound().build();

        Review review = reviewService.getByProdCust(product, customer);

        if (review==null) {
            return ResponseEntity.notFound().build();
        }

       Product updatedProduct = reviewService.updateReview(review, request, product);

        if (updatedProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(new ErrorResponse(ErrorType.DUPLICATION_OF_DATA, "Не бе извършена промяна", HttpStatus.NOT_MODIFIED.value(), ErrorMessage.DUPLICATION_OF_REVIEW_DATA));
        }

        try {
            productService.save(updatedProduct);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok("Успешно променено ревю");

    }

    @DeleteMapping("deletereview")//TODO change to review/delete
    @Transactional
    public ResponseEntity<String> deleteReview(@RequestBody CustomerProductPairRequest pairRequest) {
        Customer customer = customerService.findById(pairRequest.customerId);

        if (customer == null)
            return ResponseEntity.notFound().build();

        Product product = productService.findByPCode(pairRequest.productCode);

        if (product == null)
            return ResponseEntity.notFound().build();

        Review review = reviewService.getByProdCust(product, customer);

        if (review == null)
            return ResponseEntity.notFound().build();

        short newRating = reviewService.updatedRating(product, review);

        if (newRating == -1)
            return ResponseEntity.notFound().build();

        product.setRating(newRating);
        product.getReviews().remove(review);
        productService.save(product);
        reviewService.delete(review);

        return ResponseEntity.ok().body("Ревюто е изтрито");
    }
}
